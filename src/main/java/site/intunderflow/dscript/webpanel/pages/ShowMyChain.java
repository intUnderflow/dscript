package site.intunderflow.dscript.webpanel.pages;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import freemarker.template.Configuration;
import freemarker.template.Template;
import site.intunderflow.dscript.actors.ListeningNode;
import site.intunderflow.dscript.application.blocklattice.BlockchainTraceback;
import site.intunderflow.dscript.application.blocklattice.NetworkState;
import site.intunderflow.dscript.application.blocklattice.blockchain.address.BaseAddress;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.Block;
import site.intunderflow.dscript.utility.FreemarkerConfiguration;
import site.intunderflow.dscript.utility.TemplateMerger;
import site.intunderflow.dscript.webpanel.Webpanel;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShowMyChain implements Page {

    private final Webpanel webpanel;

    private final Configuration configuration;

    public ShowMyChain(Webpanel webpanel){
        this.webpanel = Preconditions.checkNotNull(webpanel);
        this.configuration = FreemarkerConfiguration.getConfiguration(this);
    }

    @Override
    public String getPathToMatch(){
        return "show_chain";
    }

    private Map<String, Object> getData(Request request){
        List<Block> blocks = new ArrayList<>();
        BaseAddress baseAddress;
        if (request.queryParams("address") != null){
            String address = request.queryParams("address");
            if (address.length() == 128){
                baseAddress = new BaseAddress(address);
            }
            else{
                baseAddress = webpanel.getNode().getNameService().getForName(address);
            }
        }
        else{
            baseAddress = webpanel.getCommander().getFor();
        }
        NetworkState networkState = webpanel.getNode().getNetworkState();
        new BlockchainTraceback(networkState, blocks::add)
                .trace(
                        networkState.getHead(baseAddress)
                ).blockUntilComplete();
        Map<String, Object> data = new HashMap<>();
        data.put("blocks", Lists.reverse(blocks));
        return data;
    }

    @Override
    public void accept(Request request, Response response){
        try{
            Template template = configuration.getTemplate("ShowMyChain.html");
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
