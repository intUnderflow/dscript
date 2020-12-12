package site.intunderflow.dscript.webpanel.pages;

import com.google.common.base.Preconditions;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.apache.http.client.utils.URIBuilder;
import site.intunderflow.dscript.actors.ListeningNode;
import site.intunderflow.dscript.application.blocklattice.AccountCreationHelper;
import site.intunderflow.dscript.application.blocklattice.blockchain.BlockHashFactory;
import site.intunderflow.dscript.application.blocklattice.blockchain.address.BaseAddress;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.basic_tink.Create;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.dapp_ddl.DAppCreate;
import site.intunderflow.dscript.application.executor.ddl.v1.program.InstructionSet;
import site.intunderflow.dscript.network.message.content.ExecutionRequest;
import site.intunderflow.dscript.utility.*;
import site.intunderflow.dscript.utility.crypto.keys.KeyPair;
import site.intunderflow.dscript.utility.crypto.keys.KeySerializer;
import site.intunderflow.dscript.webpanel.Webpanel;
import spark.Request;
import spark.Response;

import java.io.File;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ExecuteDApp implements Page {

    private final ListeningNode listeningNode;

    private final Configuration configuration;

    public ExecuteDApp(Webpanel webpanel){
        this.configuration = FreemarkerConfiguration.getConfiguration(this);
        this.listeningNode = Preconditions.checkNotNull(webpanel).getNode();
    }

    public String getPathToMatch(){
        return "execute_dapp";
    }

    public int getPriority(){
        return 10;
    }

    private void attemptCreate(String hexAddress, String hexInput, Map<String, Object> data){
        data.put("showForm", false);
        if (hexAddress.length() != 128){
            BaseAddress resolved = listeningNode.getNameService().getForName(hexAddress);
            if (resolved == null){
                throw new NullPointerException("Name not resolved.");
            }
            hexAddress = resolved.toString();
        }
        listeningNode.getRouter().broadcast(
                new ExecutionRequest(
                        BaseAddress.fromString(hexAddress),
                        Hex.decode(hexInput)
                ).toMessage(6)
        );
    }

    private Map<String, Object> getData(Request request){
        Map<String, Object> data = new HashMap<>();
        data.put("baseUrl", BaseUrlFinder.getBaseUrl(request));
        if (request.queryParams("execute_dapp") != null){
            attemptCreate(request.queryParams("dapp_address"), request.queryParams("hex_input"), data);
        }
        else{
            data.put("showForm", true);
        }
        return data;
    }

    public void accept(Request request, Response response){
        try{
            Template template = configuration.getTemplate("ExecuteDApp.html");
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
