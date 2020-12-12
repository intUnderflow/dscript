package site.intunderflow.dscript.utility;

import org.apache.http.client.utils.URIBuilder;
import spark.Request;

import java.net.URISyntaxException;
import java.util.ArrayList;

public class BaseUrlFinder {

    public static String getBaseUrl(Request request){
        try{
            URIBuilder uriBuilder = new URIBuilder(request.url());
            uriBuilder.setParameters(new ArrayList<>());
            return uriBuilder.build().toString();
        }
        catch(URISyntaxException e){
            throw new Error(e);
        }
    }

}
