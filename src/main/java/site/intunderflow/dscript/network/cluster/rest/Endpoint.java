package site.intunderflow.dscript.network.cluster.rest;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.json.JSONArray;
import org.json.JSONObject;
import site.intunderflow.dscript.actors.ListeningNode;
import site.intunderflow.dscript.application.dapp_website.DAppWebsiteServer;
import site.intunderflow.dscript.application.lddb.database.Database;
import site.intunderflow.dscript.application.lddb.resource.Identifier;
import site.intunderflow.dscript.network.cluster.address.AddressBook;
import site.intunderflow.dscript.network.cluster.rest.version.V1Endpoint;
import site.intunderflow.dscript.network.message.Message;
import site.intunderflow.dscript.utility.Hex;
import site.intunderflow.dscript.webpanel.Webpanel;
import spark.Request;
import spark.Response;
import spark.Service;


import java.util.Map;
import java.util.function.Consumer;

import static spark.Service.ignite;

/**
 * A RESTful endpoint for the cluster layer. Uses Spark.
 */
public class Endpoint {

    private static final int MAX_THREADS = 20;

    private final ImmutableMap<String, EndpointVersion> endpoints;

    private DAppWebsiteServer dAppWebsiteServer;

    private int port;

    private Database lddbDatabase;

    private Webpanel webpanel;

    public Endpoint(ListeningNode node){
        this(
                ImmutableList.of(
                        new V1Endpoint(node)
                )
        );
    }

    public Endpoint(AddressBook addressBook){
        this(
                ImmutableList.of(
                        new V1Endpoint(addressBook)
                )
        );
    }

    public Endpoint(

            ImmutableList<EndpointVersion> endpointVersions
    ){
        ImmutableMap.Builder<String, EndpointVersion> builder = ImmutableMap.builder();
        for (EndpointVersion endpointVersion : endpointVersions){
            builder.put(endpointVersion.getVersion(), endpointVersion);
        }
        this.endpoints = builder.build();
    }

    public void setup(int port){
        System.out.println("Starting cluster endpoint on port " + port);
        this.port = port;
        Service endpointSparkService = ignite().port(port).threadPool(MAX_THREADS);
        // We can use the wildcard support in our fork of spark.
        endpointSparkService.get("*", setupDefault());
        endpointSparkService.post("*", setupDefault());
    }

    private ConsumerSparkRoute setupDefault(){
        return new ConsumerSparkRoute(this::handleDefault);
    }

    public void setdAppWebsiteServer(DAppWebsiteServer dAppWebsiteServer){
        this.dAppWebsiteServer = Preconditions.checkNotNull(dAppWebsiteServer);
    }

    private void handleVersions(Request request, Response response){
        JSONArray versionsArray = new JSONArray();
        for (Map.Entry<String,EndpointVersion> entry : endpoints.entrySet()){
            versionsArray.put(entry.getValue().getVersion());
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("versions", versionsArray);
        response.body(jsonObject.toString());
    }

    private void notFound(Response response){
        response.body("");
        response.status(404);
    }

    private void handleLDDB(Request request, Response response){
        if (lddbDatabase == null){
            notFound(response);
            return;
        }
        String identifierString = request.queryParams("identifier");
        Identifier identifier = Identifier.fromString(Hex.decodeString(identifierString));
        String lddbAction = request.queryParamOrDefault("__lddbaction", "get");
        if (lddbAction.equals("get")) {
            byte[] data = lddbDatabase.getLocal(identifier);
            if (data.length == 0){
                notFound(response);
                return;
            }
            response.body(Hex.encode(data));
        }
        else if(lddbAction.equals("query_can_fulfill")){
            boolean canFulfill = lddbDatabase.checkIfIdentifierPendingDirectFind(identifier);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("success", true);
            jsonObject.put("canFulfill", canFulfill);
            response.body(jsonObject.toString());
        }
        else if(lddbAction.equals("fulfill")){
            String content = request.queryParams("__lddbaction_content");
            String encoding = request.queryParams("__lddbaction_encoding");
            if (encoding.equals("hex")){
                byte[] contentBytes = Hex.decode(content);
                boolean isForData = identifier.isForData(contentBytes);
                if (!isForData){
                    // Invalid data.
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("success", false);
                    jsonObject.put("error", "Data corrupted, does not match identifier.");
                    response.body(jsonObject.toString());
                }
                else{
                    lddbDatabase.fulfillDirectFinds(identifier, contentBytes);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("success", true);
                    response.body(jsonObject.toString());
                }
            }
            else{
                //Can't support encoding.
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("success", false);
                jsonObject.put("error", "Encoding not supported: " + encoding);
                JSONArray supported = new JSONArray();
                supported.put("hex");
                jsonObject.put("supported_encodings", supported);
                response.body(jsonObject.toString());
            }
        }
    }

    private void handleWebPanel(Request request, Response response){
        if (webpanel == null){
            // We don't want to give away there's no panel so just reject.
            response.body("");
            response.status(403);
        }
        else{
            webpanel.accept(request, response);
        }
    }

    private void ping(Request request, Response response){
        response.body("pong");
    }

    private void handleDefault(Request request, Response response){
        try {
            String path = request.pathInfo();
            if (request.host().matches(".+\\.dscript\\.site:*.*") && !request.host().startsWith("my.dscript.site")){
                if (dAppWebsiteServer != null){
                    dAppWebsiteServer.accept(request, response);
                    return;
                }
                else{
                    System.out.println("No website server attached.");
                    response.status(404);
                    response.body("");
                    return;
                }
            }
            if (path.startsWith("/ui")) {
                handleWebPanel(request, response);
                return;
            }
            if (path.equals("/versions")) {
                handleVersions(request, response);
                return;
            }
            if (path.equals("/ping")) {
                ping(request, response);
                return;
            }
            if (path.startsWith("/lddb")) {
                handleLDDB(request, response);
                return;
            }
            for (ImmutableMap.Entry<String, EndpointVersion> entry : endpoints.entrySet()) {
                if (path.startsWith("/" + entry.getKey())) {
                    entry.getValue().handle(request, response);
                    return;
                }
            }
            response.status(404);
            response.body("");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void setMessageHandler(Consumer<Message> messageHandler){
        for (ImmutableMap.Entry<String, EndpointVersion> entry : endpoints.entrySet()){
            entry.getValue().setMessageHandler(messageHandler);
        }
    }

    public int getPort() {
        return Preconditions.checkNotNull(port);
    }

    public void setLDDBDatabase(Database lddbDatabase){
        this.lddbDatabase = lddbDatabase;
    }

    public void setWebPanel(Webpanel webPanel){
        this.webpanel = webPanel;
        webPanel.notifyEndpointAttached(this);
    }

}
