package site.intunderflow.dscript.network.message;

import com.google.common.base.Preconditions;

import java.util.Arrays;

public class MessageForComparison {

    private final Message message;

    public MessageForComparison(Message message){
        this.message = Preconditions.checkNotNull(message);
    }

    @Override
    public boolean equals(Object o){
        if (o == null){
            return false;
        }
        else if (getClass() != o.getClass()){
            return false;
        }
        else{
            return doCompare((MessageForComparison) o);
        }
    }

    private Message getMessage(){
        return message;
    }

    private boolean doCompare(MessageForComparison compare){
        return compareWork(compare) && compareContent(compare);
    }

    private boolean compareWork(MessageForComparison compare){
        return Arrays.equals(
                compare.getMessage().getWork(),
                message.getWork()
        );
    }

    private boolean compareContent(MessageForComparison compare){
        return message.getContentString().equals(compare.getMessage().getContentString());
    }

}
