package site.intunderflow.dscript.network.message.content;

import com.google.common.base.Preconditions;
import org.json.JSONObject;
import site.intunderflow.dscript.application.lddb.messages.Broadcast;
import site.intunderflow.dscript.network.message.Message;
import site.intunderflow.dscript.network.message.MessageContent;
import site.intunderflow.dscript.network.message.MessageWithReachFactory;

public class LDDBBroadcast implements MessageContent {

    private final Broadcast broadcast;

    public LDDBBroadcast(
            Broadcast broadcast
    ){
        this.broadcast = Preconditions.checkNotNull(broadcast);
    }

    @Override
    public String toString(){
        JSONObject jsonObject = new JSONObject(broadcast.toString());
        jsonObject.put("type", getType());
        return jsonObject.toString();
    }

    @Override
    public Message toMessage(int reach){
        return new MessageWithReachFactory(this).create(reach);
    }

    @Override
    public String getType(){
        return this.getClass().getSimpleName();
    }

    public Broadcast getBroadcast(){
        return broadcast;
    }

    public static LDDBBroadcast fromString(String from){
        return new LDDBBroadcast(
                Broadcast.fromString(from)
        );
    }

}
