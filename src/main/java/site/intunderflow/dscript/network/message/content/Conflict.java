package site.intunderflow.dscript.network.message.content;

import com.google.common.base.Preconditions;
import org.json.JSONObject;
import site.intunderflow.dscript.application.blocklattice.blockchain.BlockFromBlockStringFactory;
import site.intunderflow.dscript.application.blocklattice.blockchain.BlockStringFromBytesFactory;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.Block;
import site.intunderflow.dscript.network.message.Message;
import site.intunderflow.dscript.network.message.MessageContent;
import site.intunderflow.dscript.network.message.MessageWithReachFactory;
import site.intunderflow.dscript.utility.Hex;

import javax.crypto.ExemptionMechanismException;

/**
 * Declares a conflict on a given block by showing at least two blocks referencing it (prevents attackers
 * just making fake conflicts).
 */
public class Conflict implements MessageContent {

    private final byte[] previous;

    private final Block firstBlock;

    private final Block secondBlock;

    public Conflict(
            byte[] previous,
            Block firstBlock,
            Block secondBlock
    ){
        this.previous = Preconditions.checkNotNull(previous);
        this.firstBlock = Preconditions.checkNotNull(firstBlock);
        this.secondBlock = Preconditions.checkNotNull(secondBlock);
    }

    @Override
    public String getType(){
        return this.getClass().getSimpleName();
    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", getType());
        jsonObject.put("previous", Hex.encode(previous));
        jsonObject.put("first", Hex.encode(firstBlock.toBytes()));
        jsonObject.put("second", Hex.encode(secondBlock.toBytes()));
        return jsonObject.toString();
    }

    @Override
    public Message toMessage(int reach){
        return new MessageWithReachFactory(this).create(reach);
    }

    public static Conflict fromString(String from){
        JSONObject jsonObject = new JSONObject(from);
        try {
            return new Conflict(
                Hex.decode(jsonObject.getString("previous")),
                new BlockFromBlockStringFactory(
                        new BlockStringFromBytesFactory(
                                Hex.decode(jsonObject.getString("first"))
                        ).getString()
                ).getBlock(),
                new BlockFromBlockStringFactory(
                        new BlockStringFromBytesFactory(
                                Hex.decode(jsonObject.getString("second"))
                        ).getString()
                ).getBlock()
            );
        }
        catch(Exception e){
            return null;
        }
    }

    public byte[] getPrevious() {
        return previous;
    }

    public Block getFirstBlock() {
        return firstBlock;
    }

    public Block getSecondBlock() {
        return secondBlock;
    }
}
