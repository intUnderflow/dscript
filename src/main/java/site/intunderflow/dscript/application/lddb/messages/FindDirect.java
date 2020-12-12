package site.intunderflow.dscript.application.lddb.messages;

import com.google.common.base.Preconditions;
import org.json.JSONObject;
import site.intunderflow.dscript.application.lddb.resource.Identifier;
import site.intunderflow.dscript.application.lddb.resource.Location;
import site.intunderflow.dscript.network.message.content.LDDBFindDirect;

public class FindDirect implements LDDBMessage {

    private final Identifier identifier;

    private final Location reportTo;

    public FindDirect(Identifier identifier, Location reportTo){
        this.identifier = Preconditions.checkNotNull(identifier);
        this.reportTo = Preconditions.checkNotNull(reportTo);
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public Location getReportTo() {
        return reportTo;
    }

    public String toString(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("identifier", identifier.toString());
        jsonObject.put("report-to", reportTo.toString());
        return jsonObject.toString();
    }

    public static FindDirect fromString(String from){
        JSONObject jsonObject = new JSONObject(from);
        return new FindDirect(
                Identifier.fromString(
                        jsonObject.getString("identifier")
                ),
                Location.fromString(
                        jsonObject.getString("report-to")
                )
        );
    }

    public LDDBFindDirect getMessageContent(){
        return new LDDBFindDirect(this);
    }

}
