package site.intunderflow.dscript.network.cluster;

import com.google.common.base.Preconditions;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import site.intunderflow.dscript.network.cluster.address.Address;
import site.intunderflow.dscript.network.cluster.address.AddressBook;
import site.intunderflow.dscript.network.cluster.address.network.NetworkAddress;
import site.intunderflow.dscript.network.message.Message;
import site.intunderflow.dscript.utility.Base64;
import site.intunderflow.dscript.utility.Flag;
import site.intunderflow.dscript.utility.Hex;
import site.intunderflow.dscript.utility.Time;
import site.intunderflow.dscript.work.BasicSHA512Work;

import javax.print.URIException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for providing methods to interact with a node given its address.
 */
public class NodeInterface {

    private static final String DEFAULT_PROTOCOL = "http";

    private static final Logger logger = Logger.getLogger(NodeInterface.class.getName());

    private final Address address;

    private final CloseableHttpClient httpClient;

    private AddressBook addressBook = null;

    private int difficultyRequired = -1;

    private String self;

    public NodeInterface(Address address) {
        this.address = Preconditions.checkNotNull(address);
        this.httpClient = HttpClients.createDefault();
    }

    private void seenRecently(){
        if (this.addressBook != null){
            this.addressBook.addAddressIfNewer(address, Time.getUTCTimestamp());
        }
    }

    private String getWorkString(String uri) {
        return Hex.encode(getWork(uri));
    }

    private byte[] getWork(String uri) {
        if (difficultyRequired == -1) {
            try {
                difficultyRequired = fetchWorkDifficulty();
            } catch (RequestException e) {
                throw new Error(e);
            }
        }
        BasicSHA512Work worker = new BasicSHA512Work(uri);
        return worker.performWork(difficultyRequired);
    }

    private String getBody(HttpResponse response) throws IOException{
        return IOUtils.toString(
                response.getEntity().getContent(),
                StandardCharsets.UTF_8
        );
    }

    private String getAddressWithPath(String path) {
        String addressString = address.toString();
        char lastChar = addressString.charAt(addressString.length() - 1);
        if (lastChar != '/' && path.charAt(0) != '/') {
            return addressString + "/" + path;
        } else {
            return addressString + path;
        }
    }

    private String getPathWithProtocol(String path) {
        return DEFAULT_PROTOCOL + "://" + path;
    }

    private String getAddressWithPathAndProtocol(String path) {
        String addressWithPathAndProtocol = getPathWithProtocol(getAddressWithPath(path));
        return addressWithPathAndProtocol;
    }

    private URI getUriWithWork(String uri, String work) throws URISyntaxException {
        return getUriBuilderWithWork(uri, work).build();
    }

    private URIBuilder getUriBuilderWithWork(String uri, String work) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(uri);
        String workString = getWorkString(work);
        logger.log(Level.FINE, uri);
        uriBuilder.addParameter("work", workString);
        return uriBuilder;
    }

    private HttpGet getHttpGetWithWork(String uri, String work) throws URISyntaxException {
        return new HttpGet(getUriWithWork(uri, work));
    }

    private int fetchWorkDifficulty() throws RequestException {
        String uri = getAddressWithPathAndProtocol(
                "/v1/work"
        );
        // We do NOT USE work as this finds the work needed.
        HttpGet httpGet = new HttpGet(uri);
        try {
            CloseableHttpResponse response = httpClient.execute(httpGet);
            String responseBody = getBody(response);
            JSONObject jsonObject = new JSONObject(responseBody);
            response.close();
            int work = jsonObject.getInt("work");
            seenRecently();
            return work;
        } catch (Exception e) {
            throw new RequestException(e);
        }
    }

    private AddressBook getAddressesFromEndpoint(String endpoint) throws RequestException {
        String uri = getAddressWithPathAndProtocol(
                endpoint
        );
        logger.log(Level.FINE, uri);
        HttpGet httpGet;
        try {
            httpGet = getHttpGetWithWork(uri, endpoint);
        } catch (URISyntaxException e) {
            throw new RequestException(e);
        }
        AddressBook addressBook = new AddressBook();
        Flag failed = new Flag();
        try {
            CloseableHttpResponse response = httpClient.execute(httpGet);
            String responseBody = getBody(response);
            response.close();
            JSONObject responseJSONObject = new JSONObject(responseBody);
            JSONArray addressesArray = responseJSONObject.getJSONArray("addresses");
            for (int i = 0; i < addressesArray.length(); i++) {
                JSONObject addressObject = addressesArray.getJSONObject(i);
                String address = addressObject.getString("address");
                long lastSeen = addressObject.getLong("lastSeen");
                addressBook.addAddress(
                        Address.fromString(
                                address
                        ),
                        lastSeen
                );
            }
        } catch (Exception e) {
            failed.raise();
            throw new RequestException(e);
        }
        if (!failed.isRaised()){
            seenRecently();
        }
        return addressBook;
    }

    public void attemptPing(){
        try{
            ping();
        }
        catch(RequestException e){}
    }

    public void ping() throws RequestException{
        String uri = getAddressWithPathAndProtocol("/ping");
        HttpGet httpGet = new HttpGet(uri);
        try{
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            String responseBody = getBody(httpResponse);
            httpResponse.close();
            if (!responseBody.equals("pong")){
                throw new RequestException("Unexpected output from ping (expected pong)");
            }
            seenRecently();
        }
        catch(Exception e){
            throw new RequestException(e);
        }
    }

    public List<byte[]> getFrontiers() throws RequestException{
        String uri = getAddressWithPathAndProtocol("/v1/frontiers");
        HttpGet httpGet;
        try{
            httpGet = getHttpGetWithWork(uri, "/v1/frontiers");
        }
        catch(URISyntaxException e){
            throw new RequestException(e);
        }
        List<byte[]> frontiers = new ArrayList<>();
        try{
            CloseableHttpResponse response = httpClient.execute(httpGet);
            String responseBody = getBody(response);
            response.close();
            JSONObject responseJSONObject = new JSONObject(responseBody);
            JSONArray frontiersArray = responseJSONObject.getJSONArray("frontiers");
            for (int i = 0; i < frontiersArray.length(); i++){
                String frontierHex = frontiersArray.getString(i);
                frontiers.add(Hex.decode(frontierHex));
            }
        }
        catch(Exception e){
            throw new RequestException(e);
        }
        return frontiers;
    }

    public AddressBook getAllAddresses() throws RequestException {
        return getAddressesFromEndpoint("/v1/addresses/all");
    }

    public AddressBook getAliveAddresses() throws RequestException {
        return getAddressesFromEndpoint("/v1/addresses/alive");
    }

    public void sendMessage(Message message) throws RequestException {
        String from;
        if (self != null){
            from = "FROM " + self + " ";
        }
        else{
            from = "";
        }
        //System.out.println(from + "TO " + address.toString() + ": " + message.getContentString());
        Preconditions.checkArgument(
                message.getWorkForBroadcast() > 0,
                "Message is not suitable to broadcast because the work is zero or lower."
        );
        HttpGet httpGet;
        try {
            URIBuilder uriBuilder = getUriBuilderWithWork(
                    getAddressWithPathAndProtocol("/v1/messages"),
                    "/v1/messages"
            );
            uriBuilder.addParameter("message", Base64.encode(message.toString()));
            httpGet = new HttpGet(
                    uriBuilder.build()
            );
            httpClient.execute(httpGet).close();
            seenRecently();
        } catch (Exception e) {
            throw new RequestException(e);
        }
    }

    public void discoverAddresses(AddressBook discoverInto) throws RequestException {
        AddressBook aliveAddresses = getAliveAddresses();
        aliveAddresses.mergeInto(discoverInto);
    }

    public Address getOwnAddress(int withPort, String withPath) throws RequestException{
        HttpGet httpGet;
        try{
            httpGet = getHttpGetWithWork(
                    getAddressWithPathAndProtocol("/v1/myAddress"),
                    "/v1/myAddress"
            );
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            String responseBody = getBody(httpResponse);
            JSONObject responseJson = new JSONObject(responseBody);
            httpResponse.close();
            Address address =  new Address(
                    NetworkAddress.fromString(responseJson.getString("ip")),
                    withPort,
                    withPath
            );
            seenRecently();
            return address;
        }
        catch(Exception e){
            throw new RequestException(e);
        }
    }

    public void setSelf(String self){
        this.self = self;
    }

    public NodeInterface setAddressBook(AddressBook addressBook){
        this.addressBook = Preconditions.checkNotNull(addressBook);
        return this;
    }

}
