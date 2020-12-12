package site.intunderflow.dscript.network.message.content;

import com.google.common.base.Preconditions;
import org.json.JSONObject;
import site.intunderflow.dscript.application.bcra.commitment.ExecutorCommitment;
import site.intunderflow.dscript.network.message.Message;
import site.intunderflow.dscript.network.message.MessageContent;
import site.intunderflow.dscript.network.message.MessageWithReachFactory;

public class BCRACommitment implements MessageContent {

    private final ExecutorCommitment executorCommitment;

    public BCRACommitment(
            ExecutorCommitment executorCommitment
    ){
        this.executorCommitment = Preconditions.checkNotNull(executorCommitment);
    }

    @Override
    public Message toMessage(int reach){
        return new MessageWithReachFactory(this).create(reach);
    }

    @Override
    public String getType(){
        return this.getClass().getSimpleName();
    }

    @Override
    public String toString(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", getType());
        jsonObject.put("executor", executorCommitment.toString());
        return jsonObject.toString();
    }

    public ExecutorCommitment getExecutorCommitment() {
        return executorCommitment;
    }

    public static BCRACommitment fromString(String from){
        JSONObject jsonObject = new JSONObject(from);
        return new BCRACommitment(
                ExecutorCommitment.fromString(
                        jsonObject.getString("executor")
                )
        );
    }
}
