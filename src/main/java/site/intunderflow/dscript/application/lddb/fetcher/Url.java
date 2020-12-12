package site.intunderflow.dscript.application.lddb.fetcher;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import site.intunderflow.dscript.application.lddb.resource.Location;
import site.intunderflow.dscript.utility.Hex;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Url implements Fetcher {

    private static final HttpClient httpClient = HttpClients.createDefault();

    private static final Logger logger = Logger.getLogger(Fetcher.class.getName());

    @Override
    public String getType(){
        return "url";
    }

    @Override
    public byte[] fetch(Location location){
        HttpGet httpGet = new HttpGet(
                location.getLocation()
        );
        byte[] content = new byte[0];
        try{
            HttpResponse httpResponse = httpClient.execute(httpGet);
            content = IOUtils.toByteArray(httpResponse.getEntity().getContent());
            httpGet.releaseConnection();
        }
        catch(Exception e){
            logger.log(Level.WARNING, e.getMessage());
        }
        return Hex.decode(new String(content, StandardCharsets.UTF_8));
    }

    @Override
    public boolean isRemote(){
        return true;
    }

    @Override
    public boolean canWeFulfill(Location location){
        HttpGet httpGet;
        try {
            httpGet = new HttpGet(
                    new URIBuilder(location.getLocation())
                    .addParameter("__lddbaction", "query_can_fulfill")
                    .build()
            );
        }
        catch(URISyntaxException e){
            // URL is guaranteed to meet convention standards, impossible state.
            throw new Error(e);
        }
        try{
            HttpResponse httpResponse = httpClient.execute(httpGet);
            String responseString = IOUtils.toString(
                    httpResponse.getEntity().getContent(),
                    StandardCharsets.UTF_8
            );
            httpGet.releaseConnection();
            JSONObject jsonObject = new JSONObject(responseString);
            return jsonObject.getBoolean("canFulfill");
        }
        catch(Exception e){
            logger.log(Level.WARNING, e.getMessage());
            return false;
        }
    }

    @Override
    public void attemptToFulfill(Location location, byte[] content){
        HttpGet httpGet;
        try{
            URI uri = new URIBuilder(location.getLocation())
                .addParameter("__lddbaction", "fulfill")
                .addParameter("__lddbaction_content", Hex.encode(content))
                .addParameter("__lddbaction_encoding", "hex")
                .build();
            httpGet = new HttpGet(
                    uri
            );
        }
        catch(URISyntaxException e){
            // URL is guaranteed to meet convention standards, impossible state.
            throw new Error(e);
        }
        try{
            httpClient.execute(httpGet);
            httpGet.releaseConnection();
        }
        catch(IOException e){
            logger.log(Level.WARNING, e.getMessage());
        }
    }
}
