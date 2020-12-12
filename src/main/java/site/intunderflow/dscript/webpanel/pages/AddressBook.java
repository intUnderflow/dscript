package site.intunderflow.dscript.webpanel.pages;

import com.google.common.base.Preconditions;
import freemarker.template.Configuration;
import freemarker.template.Template;
import site.intunderflow.dscript.actors.ListeningNode;
import site.intunderflow.dscript.utility.FreemarkerConfiguration;
import site.intunderflow.dscript.utility.TemplateMerger;
import site.intunderflow.dscript.webpanel.Webpanel;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddressBook implements Page {

    private final ListeningNode node;

    private final Configuration configuration;

    public AddressBook(Webpanel webpanel){
        this.node = Preconditions.checkNotNull(webpanel).getNode();
        this.configuration = FreemarkerConfiguration.getConfiguration(this);
    }

    @Override
    public String getPathToMatch(){
        return "address_book";
    }

    private Map<String, Object> getData(){
        List<String> addresses = new ArrayList<>();
        node.getAddressBook().getAllAddresses().forEach((address) -> {
            addresses.add(address.toString());
        });
        Map<String, Object> data = new HashMap<>();
        data.put("addresses", addresses);
        return data;
    }

    @Override
    public void accept(Request request, Response response){
        try{
            Template template = configuration.getTemplate("AddressBook.html");
            response.body(
                    TemplateMerger.getForTemplate(
                            template, getData()
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
