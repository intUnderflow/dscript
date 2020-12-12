package site.intunderflow.dscript.application.lddb.messages;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.network.message.Message;
import site.intunderflow.dscript.network.message.MessageContent;
import site.intunderflow.dscript.network.message.content.LDDBBroadcast;
import site.intunderflow.dscript.network.message.content.LDDBFindDirect;

public class NetworkMessageContentFactory {

    private final LDDBMessage message;

    public NetworkMessageContentFactory(
            LDDBMessage message
    ){
        this.message = Preconditions.checkNotNull(message);
    }

    public MessageContent toMessage(){
        String className = message.getClass().getSimpleName();
        switch(className){
            case "Broadcast":
                return new LDDBBroadcast(
                        (Broadcast) message
                );
            case "FindDirect":
                return new LDDBFindDirect(
                        (FindDirect) message
                );
            default:
                return null;
        }
    }

}
