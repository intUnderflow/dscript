package site.intunderflow.dscript.webpanel.pages;

import com.google.common.base.Preconditions;
import freemarker.template.Configuration;
import freemarker.template.Template;
import site.intunderflow.dscript.application.blocklattice.blockchain.address.BaseAddress;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.TransferBlock;
import site.intunderflow.dscript.utility.FixedLengthConveyor;
import site.intunderflow.dscript.utility.FreemarkerConfiguration;
import site.intunderflow.dscript.utility.RandomString;
import site.intunderflow.dscript.utility.TemplateMerger;
import site.intunderflow.dscript.webpanel.Webpanel;
import spark.Request;
import spark.Response;

import java.util.HashMap;
import java.util.Map;

public class Transfer implements Page {

    private final Webpanel webpanel;

    private final Configuration configuration;

    private final FixedLengthConveyor<String> doubleSpendPrevention;

    public Transfer(Webpanel webpanel){
        this.webpanel = Preconditions.checkNotNull(webpanel);
        this.configuration = FreemarkerConfiguration.getConfiguration(this);
        this.doubleSpendPrevention = new FixedLengthConveyor<>(20);
    }

    @Override
    public String getPathToMatch(){
        return "send_transfer";
    }

    private Map<String, Object> getData(Request request){
        if (!webpanel.hasCommander()){
            return new HashMap<>();
        }
        Map<String, Object> data = new HashMap<>();
        data.put("doublespend", RandomString.getRandomString(20));
        data.put("availableBalance", webpanel.getCommander().getBalance());
        if (request.queryParams("doublespend_prevent") != null){
            if (doubleSpendPrevention.contains(request.queryParams("doublespend_prevent"))) {
                data.put("error", "You refreshed the page, preventing double spend.");
            }
            else {
                String addressOrName = request.queryParams("to");
                long amount = Long.valueOf(request.queryParams("amount"));
                if (amount > 0) {
                    BaseAddress address;
                    if (addressOrName.length() == 128) {
                        address = new BaseAddress(addressOrName);
                    } else {
                        address = webpanel.getNode().getNameService().getForName(addressOrName);
                    }
                    if (address != null) {
                        try {
                            TransferBlock transferBlock = webpanel.getCommander().createTransfer(address, amount);
                            webpanel.getNode().getLddb().broadcastNewData(transferBlock.toBytes());
                            data.put("transferred", true);
                            doubleSpendPrevention.add(request.queryParams("doublespend_prevent"));
                        } catch (Exception e) {
                            data.put("error", e.getMessage());
                        }
                    }
                }
            }
        }
        return data;
    }

    @Override
    public void accept(Request request, Response response){
        try{
            Template template = configuration.getTemplate("Transfer.html");
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

    @Override
    public int getPriority(){
        return 10;
    }

}
