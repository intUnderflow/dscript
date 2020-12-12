package site.intunderflow.dscript.application.blocklattice.blockchain.block.basic_tink;

import com.google.common.base.Preconditions;
import org.json.JSONObject;
import site.intunderflow.dscript.application.blocklattice.NetworkState;
import site.intunderflow.dscript.application.blocklattice.blockchain.BlockBytesFromStringFactory;
import site.intunderflow.dscript.application.blocklattice.blockchain.BlockHashFactory;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.ReceiveBlock;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.TransferBlock;
import site.intunderflow.dscript.utility.Hex;
import site.intunderflow.dscript.utility.Time;

public class Receive extends ReceiveBlock {

    private final byte[] previous;

    private final byte[] transferBlock;

    private final long amount;

    private final long utcTimestamp;

    public Receive(
        byte[] previous,
        byte[] transferBlock,
        long amount
    ){
        this(previous, transferBlock, amount, Time.getUTCTimestampInSeconds());
    }

    private Receive(
        byte[] previous,
        byte[] transferBlock,
        long amount,
        long utcTimestamp
    ){
        this.previous = Preconditions.checkNotNull(previous);
        this.transferBlock = Preconditions.checkNotNull(transferBlock);
        Preconditions.checkArgument(amount > 0);
        this.amount = amount;
        this.utcTimestamp = utcTimestamp;
    }

    public byte[] getTransferBlock() {
        return transferBlock;
    }

    public long getAmountReceived() {
        return amount;
    }

    public String getType(){
        return "receive";
    }

    public String toString(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", getType());
        jsonObject.put("account", getAccountType());
        jsonObject.put("previous", Hex.encode(getPreviousReference()));
        jsonObject.put("transfer", Hex.encode(transferBlock));
        jsonObject.put("amount", amount);
        jsonObject.put("timestamp", utcTimestamp);
        return jsonObject.toString();
    }

    public long getUTCTimestampInSeconds(){
        return utcTimestamp;
    }

    public byte[] toBytes(){
        return new BlockBytesFromStringFactory(toString()).getBytes();
    }

    public byte[] getPreviousReference(){
        return previous;
    }

    public boolean isGenesis(){
        return false;
    }

    public String getAccountType(){
        return Create.getAccountTypeStatic();
    }

    public static Receive fromString(String from) {
        JSONObject jsonObject = new JSONObject(from);
        return new Receive(
                Hex.decode(
                        jsonObject.getString("previous")
                ),
                Hex.decode(
                        jsonObject.getString("transfer")
                ),
                jsonObject.getLong("amount"),
                jsonObject.getLong("timestamp")
        );
    }

    public static Receive fromTransfer(NetworkState networkState, TransferBlock transfer){
        return new Receive(
                networkState.getHead(transfer.getAddressTo()),
                new BlockHashFactory(transfer).hash(),
                transfer.getAmount()
        );
    }

}
