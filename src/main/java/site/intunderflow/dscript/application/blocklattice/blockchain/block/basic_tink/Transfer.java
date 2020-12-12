package site.intunderflow.dscript.application.blocklattice.blockchain.block.basic_tink;

import com.google.common.base.Preconditions;
import org.json.JSONObject;
import site.intunderflow.dscript.application.blocklattice.blockchain.BlockBytesFromStringFactory;
import site.intunderflow.dscript.application.blocklattice.blockchain.BlockHashFactory;
import site.intunderflow.dscript.application.blocklattice.blockchain.address.BaseAddress;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.Block;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.TransferBlock;
import site.intunderflow.dscript.application.consensus.dpos.SignatureFactory;
import site.intunderflow.dscript.utility.Hex;
import site.intunderflow.dscript.utility.Time;
import site.intunderflow.dscript.utility.crypto.keys.PrivateKey;

import java.security.GeneralSecurityException;

public class Transfer extends TransferBlock {

    private final byte[] previousBlock;

    private final BaseAddress addressTo;

    private final long amount;

    private final long utcTimestamp;

    private byte[] signature;

    public Transfer(Block previousBlock, BaseAddress addressTo, long amount){
        this(
                new BlockHashFactory(previousBlock).hash(),
                addressTo,
                amount
        );
    }

    public Transfer(Block previousBlock, BaseAddress addressTo, long amount, PrivateKey privateKey){
        this(
                new BlockHashFactory(previousBlock).hash(),
                addressTo,
                amount,
                privateKey
        );
    }

    public Transfer(byte[] previousBlock, BaseAddress addressTo, long amount, PrivateKey privateKeyToSignWith){
        this(previousBlock, addressTo, amount);
        try{
            signWith(privateKeyToSignWith);
        }
        catch(GeneralSecurityException e){
            signature = new byte[0];
        }
    }

    public Transfer(byte[] previousBlock, BaseAddress addressTo, long amount, long timestamp){
        this.previousBlock = Preconditions.checkNotNull(previousBlock);
        this.addressTo = Preconditions.checkNotNull(addressTo);
        Preconditions.checkArgument(amount > 0);
        this.amount = amount;
        this.signature = new byte[0];
        this.utcTimestamp = timestamp;
    }

    public Transfer(byte[] previousBlock, BaseAddress addressTo, long amount){
        this(previousBlock, addressTo, amount, Time.getUTCTimestampInSeconds());
    }

    private Transfer(byte[] previousBlock, BaseAddress addressTo, long amount, byte[] signature){
        this(previousBlock, addressTo, amount);
        this.signature = Preconditions.checkNotNull(signature);
    }

    private Transfer(byte[] previousBlock, BaseAddress addressTo, long amount, byte[] signature, long timestamp){
        this(previousBlock, addressTo, amount, timestamp);
        this.signature = Preconditions.checkNotNull(signature);
    }

    public void signWith(PrivateKey privateKey) throws GeneralSecurityException {
        signature = privateKey.sign(
                new BlockBytesFromStringFactory(getTransactionComponent()).getBytes()
        );
    }

    public void signWith(SignatureFactory signatureFactory) throws GeneralSecurityException {
        signature = signatureFactory.sign(
                new BlockBytesFromStringFactory(getTransactionComponent()).getBytes()
        ).getSignature();
    }

    public long getUTCTimestampInSeconds(){
        return utcTimestamp;
    }

    public byte[] getPreviousReference() {
        return previousBlock;
    }

    public BaseAddress getAddressTo() {
        return addressTo;
    }

    public long getAmount() {
        return amount;
    }

    public String getType(){
        return "transfer";
    }

    public String getAuthorizationType(){
        return "signature";
    }

    public byte[] getAuthorization(){
        return signature;
    }

    public String toString(){
        JSONObject jsonObject = getRawTransaction();
        jsonObject.put("authorization_type", getAuthorizationType());
        jsonObject.put("authorization_signature", Hex.encode(getAuthorization()));
        return jsonObject.toString();
    }

    public byte[] toBytes(){
        return new BlockBytesFromStringFactory(toString()).getBytes();
    }

    public boolean isGenesis(){
        return false;
    }

    private JSONObject getRawTransaction(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", getType());
        jsonObject.put("account", getAccountType());
        jsonObject.put("previous", Hex.encode(previousBlock));
        jsonObject.put("to", addressTo.getAddress());
        jsonObject.put("amount", getAmount());
        jsonObject.put("timestamp", utcTimestamp);
        return jsonObject;
    }

    public String getTransactionComponent(){
        return getRawTransaction().toString();
    }

    public String getAccountType(){
        return Create.getAccountTypeStatic();
    }

    public static Transfer fromString(String from) {
        JSONObject jsonObject = new JSONObject(from);
        byte[] signature;
        if (jsonObject.has("authorization_signature")) {
            signature = Hex.decode(jsonObject.getString("authorization_signature"));
        } else {
            signature = new byte[0];
        }
        return new Transfer(
                Hex.decode(jsonObject.getString("previous")),
                new BaseAddress(jsonObject.getString("to")),
                jsonObject.getLong("amount"),
                signature,
                jsonObject.getLong("timestamp")
        );
    }

}
