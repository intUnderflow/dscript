package site.intunderflow.dscript.network.message.content;

import com.google.common.base.Preconditions;
import org.json.JSONObject;
import site.intunderflow.dscript.application.lddb.messages.FindDirect;
import site.intunderflow.dscript.network.message.Message;
import site.intunderflow.dscript.network.message.MessageContent;
import site.intunderflow.dscript.network.message.MessageWithReachFactory;

public class LDDBFindDirect implements MessageContent {

    private final FindDirect findDirect;

    public LDDBFindDirect(
            FindDirect findDirect
    ){
        this.findDirect = Preconditions.checkNotNull(findDirect);
    }

    @Override
    public String toString(){
        JSONObject jsonObject = new JSONObject(findDirect.toString());
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

    public FindDirect getFindDirect(){
        return findDirect;
    }

    public static LDDBFindDirect fromString(String from){
        return new LDDBFindDirect(
                FindDirect.fromString(from)
        );
    }

}
