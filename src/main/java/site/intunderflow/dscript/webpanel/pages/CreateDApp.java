package site.intunderflow.dscript.webpanel.pages;

import com.google.common.base.Preconditions;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;
import site.intunderflow.dscript.actors.ListeningNode;
import site.intunderflow.dscript.application.blocklattice.AccountCreationHelper;
import site.intunderflow.dscript.application.blocklattice.blockchain.BlockHashFactory;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.basic_tink.Create;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.dapp_ddl.DAppCreate;
import site.intunderflow.dscript.application.executor.ddl.v1.program.InstructionSet;
import site.intunderflow.dscript.utility.*;
import site.intunderflow.dscript.utility.crypto.keys.KeyPair;
import site.intunderflow.dscript.utility.crypto.keys.KeySerializer;
import site.intunderflow.dscript.webpanel.Webpanel;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;

import java.io.File;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CreateDApp implements Page {

    private final ListeningNode listeningNode;

    private final Configuration configuration;

    public CreateDApp(Webpanel webpanel){
        this.configuration = FreemarkerConfiguration.getConfiguration(this);
        this.listeningNode = Preconditions.checkNotNull(webpanel).getNode();
    }

    public String getPathToMatch(){
        return "create_dapp";
    }

    public int getPriority(){
        return 10;
    }

    private void attemptCreate(String hexByteCode, Map<String, Object> data, Map<String, String> files){
        data.put("showForm", false);
        InstructionSet sourceCode = InstructionSet.fromBytes(
                Hex.decode(hexByteCode)
        );
        DAppCreate createBlock = new DAppCreate(sourceCode.getVersion(), files, sourceCode);
        String address = Hex.encode(new BlockHashFactory(createBlock).hash());
        listeningNode.getLddb().broadcastNewData(createBlock.toBytes());
        data.put("address", address);
    }

    private Map<String, Object> getData(Request request){
        QueryParamsMap requestMap = request.queryMap();
        Map<String, Object> data = new HashMap<>();
        data.put("baseUrl", BaseUrlFinder.getBaseUrl(request));
        if (requestMap.value("create_dapp") != null){
            Map<String, String> files = new HashMap<>();
            String filesStr = requestMap.value("files_hex");
            if (!filesStr.equals("")){
                JSONObject filesJSON = new JSONObject(Hex.decodeString(filesStr));
                files = JSONUtils.jsonToFiles(filesJSON);
            }
            attemptCreate(requestMap.value("bytecode_hex"), data, files);
        }
        else{
            data.put("showForm", true);
        }
        return data;
    }

    public void accept(Request request, Response response){
        try{
            Template template = configuration.getTemplate("CreateDApp.html");
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
