package site.intunderflow.dscript.network.cluster.rest.version;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.json.JSONArray;
import org.json.JSONObject;
import site.intunderflow.dscript.actors.ListeningNode;
import site.intunderflow.dscript.network.cluster.address.Address;
import site.intunderflow.dscript.network.cluster.address.AddressBook;
import site.intunderflow.dscript.network.cluster.rest.EndpointVersion;
import site.intunderflow.dscript.network.message.Message;
import site.intunderflow.dscript.utility.Base64;
import site.intunderflow.dscript.utility.Filter;
import site.intunderflow.dscript.utility.Hex;
import site.intunderflow.dscript.utility.Time;
import site.intunderflow.dscript.work.BasicSHA512Work;
import site.intunderflow.dscript.work.Work;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class V1Endpoint implements EndpointVersion  {

    private static final Logger logger = Logger.getLogger(EndpointVersion.class.getName());

    private static final int WORK_DIFFICULTY_REQUIRED = 2;

    private static final String VERSION_ENDPOINT = "v1";

    private static final long RECENT_TIME = 60 * 60 * 12;

    private final AddressBook addressBook;

    private ListeningNode node;

    private Consumer<Message> messageHandler = null;

    public V1Endpoint(
            ListeningNode node
    ){
        this(node.getAddressBook());
        setNode(node);
    }

    public V1Endpoint(
            AddressBook addressBook
    ){
        this.addressBook = Preconditions.checkNotNull(addressBook);
    }

    public void setNode(ListeningNode node){
        this.node = node;
    }

    @Override
    public String getVersion(){
        return VERSION_ENDPOINT;
    }

    private boolean workSatisfied(Request request){
        return isWorkSatisfied(request, WORK_DIFFICULTY_REQUIRED);
    }

    private boolean isWorkSatisfied(Request request, int minimumWork){
        return getDifficulty(request) >= minimumWork;
    }

    private int getDifficulty(Request request){
        byte[] work;
        try {
            String workString = request.queryParams("work");
            logger.log(Level.FINER, workString);
            work = Hex.decode(workString);
        }
        catch(NullPointerException e){
            return 0;
        }
        String fullRequest = request.pathInfo();
        Work workCalculator = new BasicSHA512Work(fullRequest);
        return workCalculator.getDifficulty(work);
    }

    public void handle(
        Request request,
        Response response
    ){
        String path = request.pathInfo();
        if (path.equals("/v1/work")){
            work(request, response);
        }
        else if (!workSatisfied(request)) {
            notEnoughWork(request, response);
        }
        else {
            handleRequestAfterWork(
                request,
                response
            );
        }
    }

    private void work(Request request, Response response){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("work", WORK_DIFFICULTY_REQUIRED);
        response.body(jsonObject.toString());
    }

    private void notEnoughWork(Request request, Response response){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("error", "Not enough work done to process request.");
        jsonObject.put("workRequired", WORK_DIFFICULTY_REQUIRED);
        response.body(jsonObject.toString());
        response.status(400);
    }

    private void handleRequestAfterWork(
            Request request,
            Response response
    ){
        String path = request.pathInfo();
        if (path.equals("/v1/addresses/all")) {
            anyHeardAddress(request, response);
        } else if (path.equals("/v1/addresses/alive")) {
            anyRecentAddress(request, response);
        } else if (path.equals("/v1/myAddress")) {
            sendCallerAddress(request, response);
        } else if (path.equals("/v1/messages")) {
            handleMessage(request, response);
        } else if (path.equals("/v1/frontiers")) {
            handleFrontiers(request, response);
        }
    }

    private JSONArray getAddressesAfterFilter(Filter<Map.Entry<Address, Long>> filter){
        JSONArray addressJsonArray = new JSONArray();
        for (Map.Entry<Address, Long> entry : addressBook.getAll()){
            if (filter.shouldFilter(entry)){
                JSONObject addressJson = new JSONObject();
                addressJson.put("address", entry.getKey().toString());
                addressJson.put("lastSeen", entry.getValue());
                addressJsonArray.put(addressJson);
            }
        }
        return addressJsonArray;
    }

    private void anyHeardAddress(Request request, Response response){
        JSONArray addressJsonArray = getAddressesAfterFilter((Map.Entry<Address, Long> object) -> true);
        sendAddresses(request, response, addressJsonArray);
    }

    private void anyRecentAddress(Request request, Response response){
        long currentTime = Time.getUTCTimestamp();
        JSONArray addressJsonArray = getAddressesAfterFilter((Map.Entry<Address, Long> entry) ->
            entry.getValue() + RECENT_TIME > currentTime
        );
        sendAddresses(request, response, addressJsonArray);
    }

    private void sendAddresses(Request request, Response response, JSONArray addresses){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("addresses", addresses);
        response.body(jsonObject.toString());
    }

    private void sendCallerAddress(Request request, Response response){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("ip", request.ip());
        response.body(jsonObject.toString());
    }

    private void handleMessage(Request request, Response response){
        String messageString = request.queryParams("message");
        messageString = Base64.decode(messageString);
        Message message = Message.fromString(messageString);
        if (this.messageHandler != null){
            this.messageHandler.accept(message);
        }
        JSONObject responseJson = new JSONObject();
        responseJson.put("accepted", true);
        response.body(responseJson.toString());
    }

    private void handleFrontiers(Request request, Response response){
        List<byte[]> frontiers = node.getRealtimeMonitor().getFrontiers();
        JSONArray jsonArray = new JSONArray();
        for (byte[] frontier : frontiers){
            jsonArray.put(Hex.encode(frontier));
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("frontiers", jsonArray);
        response.body(jsonObject.toString());
    }

    @Override
    public void setMessageHandler(Consumer<Message> messageHandler){
        this.messageHandler = Preconditions.checkNotNull(messageHandler);
    }


}
