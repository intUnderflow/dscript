package site.intunderflow.dscript.webpanel.pages;

import com.google.common.base.Preconditions;
import freemarker.template.Configuration;
import freemarker.template.Template;
import site.intunderflow.dscript.application.blocklattice.blockchain.address.BaseAddress;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.NameRegistration;
import site.intunderflow.dscript.application.blocklattice.nameservice.NameService;
import site.intunderflow.dscript.utility.FreemarkerConfiguration;
import site.intunderflow.dscript.utility.TemplateMerger;
import site.intunderflow.dscript.webpanel.Webpanel;
import spark.Request;
import spark.Response;

import java.util.HashMap;
import java.util.Map;

public class EnrolFriendlyName implements Page {

    private final Configuration configuration;

    private final Webpanel webpanel;

    public EnrolFriendlyName(Webpanel webpanel){
        this.configuration = FreemarkerConfiguration.getConfiguration(this);
        this.webpanel = Preconditions.checkNotNull(webpanel);
    }

    private void attemptRegisterFriendlyName(Request request){
        BaseAddress address = new BaseAddress(request.queryParams("address"));
        String name = request.queryParams("name");
        NameRegistration nameRegistration = new NameRegistration(name, address);
        webpanel.getNode().getLddb().broadcastNewData(nameRegistration.toBytes());
    }

    private Map<String, Object> getData(Request request){
        Map<String, Object> data = new HashMap<>();
        if (webpanel.hasCommander()){
            data.put("myAddress", webpanel.getCommander().getFor().getAddress());
        }
        if (request.queryParams("register_friendly_name") != null){
            attemptRegisterFriendlyName(request);
            data.put("nameEnrolled", true);
        }
        else if (request.queryParams("query") != null){
            data.put("queryName", request.queryParams("query"));
            BaseAddress baseAddress = webpanel.getNode().getNameService()
                    .getForName(request.queryParams("query"));
            if (baseAddress != null){
                data.put("queryAddress", baseAddress.getAddress());
            }
        }
        return data;
    }

    @Override
    public String getPathToMatch(){
        return "enrol_fname";
    }

    @Override
    public void accept(Request request, Response response){
        try{
            Template template = configuration.getTemplate("EnrolFriendlyName.html");
            response.body(
                    TemplateMerger.getForTemplate(
                            template,
                            getData(request)
                    )
            );
        }
        catch(Exception e){
            response.body(e.getMessage());
        }
    }

    @Override
    public int getPriority(){
        return 10;
    }

}
