package site.intunderflow.dscript.webpanel.pages;

import com.google.common.base.Preconditions;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.apache.http.client.utils.URIBuilder;
import site.intunderflow.dscript.actors.ListeningNode;
import site.intunderflow.dscript.application.blocklattice.AccountCreationHelper;
import site.intunderflow.dscript.application.blocklattice.blockchain.BlockHashFactory;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.basic_tink.Create;
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

public class CreateAccount implements Page {

    private final ListeningNode listeningNode;

    private final Configuration configuration;

    public CreateAccount(Webpanel webpanel){
        this.configuration = FreemarkerConfiguration.getConfiguration(this);
        this.listeningNode = Preconditions.checkNotNull(webpanel).getNode();
    }

    public String getPathToMatch(){
        return "create_account";
    }

    public int getPriority(){
        return 10;
    }

    private void attemptCreateBasicTink(Map<String, Object> data){
        try {
            KeyPair keyPair = KeyPair.generateNew();
            Create created = Create.forKey(keyPair.getPublicKey());
            new AccountCreationHelper(created, listeningNode).doCreationTasks();
            String privateKey = Hex.encode(
                    KeySerializer.forPrivateKey(
                            keyPair.getPrivateKey()
                    ).serialize()
            );
            try {
                KeySerializer.forPrivateKey(
                        keyPair.getPrivateKey()
                ).serializeToFile(new File(
                        FileStorageLocation.getFileStorageLocationWithFolder("accounts") + "account_" +
                                RandomString.getRandomString(10) + ".json"
                ));
            }
            catch(Exception e){}
            data.put("keys",
                    privateKey
            );
            try {
                data.put("key_qr",
                        "data:image/png;base64,"
                                + Base64.encode(QRCode.getQRCodeDataUrl(
                                privateKey
                        )));
            }
            catch(Exception e){}
            data.put("address",
                    Hex.encode(new BlockHashFactory(created).hash())
            );
        }
        catch(GeneralSecurityException e){
            data.put("error", e.getMessage());
        }
    }

    private void attemptCreate(String accountType, Map<String, Object> data){
        data.put("showForm", false);
        if (accountType.equals("basic_tink")){
            attemptCreateBasicTink(data);
        }
        else{
            data.put("error", "Account type not found: " + accountType);
        }
    }

    private Map<String, Object> getData(Request request){
        Map<String, Object> data = new HashMap<>();
        data.put("baseUrl", BaseUrlFinder.getBaseUrl(request));
        if (request.queryParams("create_account") != null){
            attemptCreate(request.queryParams("type"), data);
        }
        else{
            data.put("showForm", true);
        }
        return data;
    }

    public void accept(Request request, Response response){
        try{
            Template template = configuration.getTemplate("CreateAccount.html");
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
