package site.intunderflow.dscript.network.message.content;

import com.google.common.base.Preconditions;
import org.json.JSONObject;
import site.intunderflow.dscript.application.blocklattice.BlockValueEvaluator;
import site.intunderflow.dscript.application.blocklattice.blockchain.BlockFromBlockStringFactory;
import site.intunderflow.dscript.application.blocklattice.blockchain.BlockStringFromBytesFactory;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.Block;
import site.intunderflow.dscript.application.consensus.dpos.Signature;
import site.intunderflow.dscript.network.message.Message;
import site.intunderflow.dscript.network.message.MessageContent;
import site.intunderflow.dscript.network.message.MessageWithReachFactory;
import site.intunderflow.dscript.utility.Hex;

public class BlockVote implements MessageContent {

    private final byte[] onIssue;

    private final Block acceptedBlock;

    private final Signature signature;

    public BlockVote(
            byte[] onIssue,
            Block acceptedBlock
    ){
        this(onIssue, acceptedBlock, null);
    }

    public BlockVote(
            byte[] onIssue,
            Block acceptedBlock,
            Signature signature
    ){
        this.onIssue = Preconditions.checkNotNull(onIssue);
        this.acceptedBlock = Preconditions.checkNotNull(acceptedBlock);
        this.signature = signature;
    }

    public byte[] getOnIssue() {
        return onIssue;
    }

    public Block getAcceptedBlock(){
        return acceptedBlock;
    }

    public Signature getSignature(){
        return signature;
    }

    @Override
    public String getType(){
        return this.getClass().getSimpleName();
    }

    private JSONObject getJSONUnsigned(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", getType());
        jsonObject.put("issue", Hex.encode(onIssue));
        jsonObject.put("accepted", Hex.encode(acceptedBlock.toBytes()));
        return jsonObject;
    }

    public String getStringToSign(){
        return getJSONUnsigned().toString();
    }

    @Override
    public String toString(){
        JSONObject jsonObject = getJSONUnsigned();
        jsonObject.put("signature", signature.toString());
        return jsonObject.toString();
    }

    public static BlockVote fromString(String content){
        JSONObject jsonObject = new JSONObject(content);
        try{
            return new BlockVote(
                Hex.decode(jsonObject.getString("issue")),
                new BlockFromBlockStringFactory(
                        new BlockStringFromBytesFactory(
                                Hex.decode(jsonObject.getString("accepted"))
                        ).getString()
                ).getBlock(),
                Signature.fromString(jsonObject.getString("signature"))
            );
        }
        catch(Exception e){
            return null;
        }
    }

    @Override
    public Message toMessage(int reach){
        return new MessageWithReachFactory(this).create(reach);
    }
}
