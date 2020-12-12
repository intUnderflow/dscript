package site.intunderflow.dscript.network.message.content;

import com.google.common.base.Preconditions;
import org.json.JSONObject;
import site.intunderflow.dscript.application.blocklattice.blockchain.BlockFromBlockStringFactory;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.DAppStateChangeBlock;
import site.intunderflow.dscript.application.consensus.dpos.Signature;
import site.intunderflow.dscript.network.message.Message;
import site.intunderflow.dscript.network.message.MessageContent;
import site.intunderflow.dscript.network.message.MessageWithReachFactory;

public class ExecutorConfirmation implements MessageContent {

    private final DAppStateChangeBlock stateChangeBlock;

    private final Signature signature;

    public ExecutorConfirmation(
            DAppStateChangeBlock dAppStateChangeBlock,
            Signature signature
    ){
        this.stateChangeBlock = Preconditions.checkNotNull(dAppStateChangeBlock);
        this.signature = Preconditions.checkNotNull(signature);
    }

    @Override
    public String getType(){
        return this.getClass().getSimpleName();
    }

    @Override
    public String toString(){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", getType());
            jsonObject.put("state", stateChangeBlock.toString());
            jsonObject.put("signature", signature.toString());
            return jsonObject.toString();
        }

    @Override
    public Message toMessage(int reach){
        return new MessageWithReachFactory(this).create(reach);
    }

    public static ExecutorConfirmation fromString(String from){
        JSONObject jsonObject = new JSONObject(from);
        return new ExecutorConfirmation(
            (DAppStateChangeBlock) new BlockFromBlockStringFactory(
                    jsonObject.getString("state")
            ).getBlock(),
            Signature.fromString(
                    jsonObject.getString("signature")
            )
        );
    }

    public Signature getSignature() {
        return signature;
    }

    public DAppStateChangeBlock getStateChangeBlock() {
        return stateChangeBlock;
    }

}
