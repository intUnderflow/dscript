package site.intunderflow.dscript.network.message;

import com.google.common.base.Preconditions;

public class MessageWithReachFactory {

    private final MessageContent content;

    public MessageWithReachFactory(MessageContent content){
        this.content = Preconditions.checkNotNull(content);
    }

    public Message create(int reach){
        return Message.buildWithWork(
                content.toString(),
                reach * 3
        );
    }

}
