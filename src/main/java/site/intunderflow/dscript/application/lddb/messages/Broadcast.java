package site.intunderflow.dscript.application.lddb.messages;

import com.google.common.base.Preconditions;
import org.json.JSONObject;
import site.intunderflow.dscript.application.lddb.resource.Identifier;
import site.intunderflow.dscript.application.lddb.resource.Location;
import site.intunderflow.dscript.network.message.content.LDDBBroadcast;

public class Broadcast implements LDDBMessage {

    private final Identifier identifier;

    private final Location location;

    private final String summary;

    public Broadcast(
                Identifier identifier,
                Location location,
                String summary
        ){
            this.identifier = Preconditions.checkNotNull(identifier);
            this.location = Preconditions.checkNotNull(location);
            this.summary = Preconditions.checkNotNull(summary);
        }

    public Broadcast(
                Identifier identifier,
                Location location
        ){
            this(identifier, location, "");
        }

    @Override
    public String toString(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("identifier", identifier.toString());
        jsonObject.put("location", location.toString());
        if (!summary.equals("")){
            jsonObject.put("summary", summary);
        }
        return jsonObject.toString();
    }

    public static Broadcast fromString(String from){
        JSONObject jsonObject = new JSONObject(from);
        String summary;
        if (jsonObject.has("summary")){
            summary = jsonObject.getString("summary");
        }
        else{
            summary = "";
        }
        return new Broadcast(
            Identifier.fromString(
                    jsonObject.getString("identifier")
            ),
            Location.fromString(
                    jsonObject.getString("location")
            ),
            summary
        );
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public Location getLocation() {
        return location;
    }

    public String getSummary() {
        return summary;
    }

    public LDDBBroadcast getMessageContent(){
        return new LDDBBroadcast(this);
    }

}
