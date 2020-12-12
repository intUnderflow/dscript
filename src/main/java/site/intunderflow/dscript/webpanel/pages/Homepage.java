package site.intunderflow.dscript.webpanel.pages;

import com.google.common.base.Preconditions;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.apache.http.client.utils.URIBuilder;
import site.intunderflow.dscript.actors.ListeningNode;
import site.intunderflow.dscript.utility.BaseUrlFinder;
import site.intunderflow.dscript.utility.FreemarkerConfiguration;
import site.intunderflow.dscript.utility.TemplateMerger;
import site.intunderflow.dscript.webpanel.Webpanel;
import spark.Request;
import spark.Response;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Homepage implements Page {

    private final Webpanel webpanel;

    private final Configuration configuration;

    public Homepage(Webpanel webpanel){
        this.configuration = FreemarkerConfiguration.getConfiguration(this);
        this.webpanel = Preconditions.checkNotNull(webpanel);
    }

    public String getPathToMatch(){
        return "*";
    }

    public int getPriority(){
        return 5;
    }

    private Map<String, Object> getData(Request request){
        Map<String, Object> data = new HashMap<>();
        data.put("baseUrl", BaseUrlFinder.getBaseUrl(request));
        data.put("addressCount", webpanel.getNode().getAddressBook().getAliveAddresses().size());
        data.put("lddbCount", webpanel.getNode().getLddb().getStorage().size());
        try {
            if (webpanel.hasCommander()) {
                data.put("balance", webpanel.getCommander().getBalance());
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return data;
    }

    public void accept(Request request, Response response){
        try{
            Template template = configuration.getTemplate("Homepage.html");
            response.body(
                    TemplateMerger.getForTemplate(
                            template, getData(request)
                    )
            );
        }
        catch(Exception e) {
            response.body(e.getMessage());
        }
    }

}
