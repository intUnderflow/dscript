package site.intunderflow.dscript.network.message;

import com.google.common.base.Preconditions;
import org.json.JSONObject;
import site.intunderflow.dscript.network.message.content.*;

public class MessageContentFromStringFactory {

    private final String content;

    public MessageContentFromStringFactory(String content){
        this.content = Preconditions.checkNotNull(content);
    }

    public MessageContent getMessageContent(){
        JSONObject jsonObject = new JSONObject(content);
        String type = jsonObject.getString("type");
        switch(type){
            case "AddressAnnouncement":
                return AddressAnnouncement.fromString(content);
            case "LDDBBroadcast":
                return LDDBBroadcast.fromString(content);
            case "LDDBFindDirect":
                return LDDBFindDirect.fromString(content);
            case "BlockVote":
                return BlockVote.fromString(content);
            case "Conflict":
                return Conflict.fromString(content);
            case "BCRACommitment":
                return BCRACommitment.fromString(content);
            case "BCRAList":
                return BCRAList.fromString(content);
            case "BCRAAffirm":
                return BCRAAffirm.fromString(content);
            case "ExecutionRequest":
                return ExecutionRequest.fromString(content);
            case "ExecutorConfirmation":
                return ExecutorConfirmation.fromString(content);
            default:
                return null;
        }
    }

}
