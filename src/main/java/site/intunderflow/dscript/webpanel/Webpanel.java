package site.intunderflow.dscript.webpanel;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import site.intunderflow.dscript.actors.ListeningNode;
import site.intunderflow.dscript.application.blocklattice.commander.Commander;
import site.intunderflow.dscript.network.cluster.rest.Endpoint;
import site.intunderflow.dscript.utility.RandomString;
import site.intunderflow.dscript.webpanel.pages.*;
import spark.Request;
import spark.Response;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Webpanel {

    private final List<String> authorizations;

    private final List<String> activeTokens;

    private final ListeningNode listeningNode;

    private int port = -1;

    private String mostRecentAuthorization;

    private ImmutableList<Page> pages;

    private Commander commander;

    public Webpanel(ListeningNode listeningNode){
        this.listeningNode = Preconditions.checkNotNull(listeningNode);
        this.authorizations = new ArrayList<>();
        this.activeTokens = new ArrayList<>();
        this.pages = ImmutableList.of(
                new Homepage(this),
                new CreateAccount(this),
                new EnrolFriendlyName(this),
                new AddressBook(this),
                new Transfer(this),
                new ShowMyChain(this),
                new CreateDApp(this),
                new ExecuteDApp(this)
        );
    }

    private String generateAuthorization(){
        return RandomString.getRandomString(30);
    }

    public void setCommander(Commander commander){
        this.commander = commander;
    }

    public boolean attemptOpenInWebBrowser(){
        if (port == -1){
            return false;
        }
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)){
            try{
                Desktop.getDesktop().browse(
                        getUrl()
                );
                return true;
            } catch(Exception e){
                return false;
            }
        }
        else{
            return false;
        }
    }

    private URI getUrl() throws URISyntaxException {
        return new URI(
                "http://my.dscript.site:"
                        + port
                        + "/ui?authorization="
                        + getAuthorization()
        );
    }

    public URI getUrlForAttemptToOpen() throws URISyntaxException {
        if (mostRecentAuthorization == null){
            mostRecentAuthorization = getAuthorization();
        }
        return new URI(
            "http://my.dscript.site:"
                    + port
                    + "/ui?authorization="
                    + mostRecentAuthorization
        );
    }

    public String getAuthorization(){
        String newAuthorization = generateAuthorization();
        authorizations.add(newAuthorization);
        mostRecentAuthorization = newAuthorization;
        return newAuthorization;
    }

    public void notifyEndpointAttached(Endpoint endpoint){
        port = endpoint.getPort();
    }

    public void accept(Request request, Response response){
        String authorization = request.queryParams("authorization");
        if (authorization != null){
            if (checkAuthorization(authorization)){
                try{
                    URIBuilder uriBuilder = new URIBuilder(request.uri());
                    removeURIBuilderParameter(uriBuilder, "authorization");
                    String redirect = uriBuilder.build().toString();
                    String token = authorizationToToken(authorization);
                    response.cookie("token", token);
                    response.body("<html><head>"
                    + "<meta http-equiv='refresh' content='1;" + redirect + "'/>"
                    + "<script type='text/javascript'>"
                    + "document.cookie='token=" + token + "';"
                    + "</script>"
                    + "</head><body><a href='" + redirect + "'>"
                    + "If your not redirected, click here."
                    + "</a></body></html");
                    response.status(200);
                    return;
                }
                catch(URISyntaxException e){
                    // Impossible because the request URL comes from a valid request.
                    throw new Error(e);
                }
            }
        }
        if (request.cookies().containsKey("token")){
            if (!checkToken(request.cookies().get("token"))){
                accessDenied(response);
                return;
            }
        }
        else{
            accessDenied(response);
            return;
        }
        acceptAuthorized(request, response);
    }

    private void acceptAuthorized(Request request, Response response){
        Page pageToUse = null;
        int currentPriority = -1;
        for (Page page : pages){
            if (page.getPathToMatch().equals("*")
                    || request.pathInfo().startsWith(page.getPathToMatch())
                    || request.pathInfo().endsWith(page.getPathToMatch())){
                if (page.getPriority() > currentPriority){
                    currentPriority = page.getPriority();
                    pageToUse = page;
                }
            }
        }
        if (pageToUse == null){
            response.status(404);
        }
        else{
            pageToUse.accept(request, response);
        }
    }

    private void removeURIBuilderParameter(URIBuilder uriBuilder, String parameter){
        List<NameValuePair> params = uriBuilder.getQueryParams();
        List<NameValuePair> newParams = new ArrayList<>();
        for (NameValuePair pair : params){
            if (!pair.getName().equals(parameter)){
                newParams.add(pair);
            }
        }
        uriBuilder.setParameters(newParams);
    }

    private String authorizationToToken(String authorization){
        authorizations.remove(authorization);
        String token = generateAuthorization();
        activeTokens.add(token);
        return token;
    }

    private boolean checkAuthorization(String authorization){
        return authorizations.contains(authorization);
    }

    private boolean checkToken(String token){
        return activeTokens.contains(token);
    }

    private void accessDenied(Response response){
        response.body("");
        response.status(403);
    }

    public boolean hasCommander(){
        return commander != null;
    }

    public Commander getCommander(){
        return commander;
    }

    public ListeningNode getNode(){
        return listeningNode;
    }

}
