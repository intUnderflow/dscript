package site.intunderflow.dscript.application.blocklattice.blockchain.block.basic_tink;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.json.JSONObject;
import site.intunderflow.dscript.application.blocklattice.blockchain.BlockBytesFromStringFactory;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.Block;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.CreateBlock;
import site.intunderflow.dscript.utility.Base64;
import site.intunderflow.dscript.utility.Hex;
import site.intunderflow.dscript.utility.Time;
import site.intunderflow.dscript.utility.crypto.keys.KeyDeserializer;
import site.intunderflow.dscript.utility.crypto.keys.KeySerializer;
import site.intunderflow.dscript.utility.crypto.keys.PublicKey;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

/** Genesis block of a basic tink account. */
public class Create extends CreateBlock {

    private final PublicKey publicKey;

    private final long utcTimestamp;

    private Create(PublicKey publicKey){
        this(publicKey, Time.getUTCTimestampInSeconds());
    }

    private Create(PublicKey publicKey, long utcTimestamp){
        this.publicKey = Preconditions.checkNotNull(publicKey);
        this.utcTimestamp = utcTimestamp;
    }

    public String toString(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", getType());
        jsonObject.put("account", getAccountType());
        jsonObject.put("key", Hex.encode(
                new KeySerializer(
                        publicKey
                ).serialize()
        ));
        jsonObject.put("timestamp", utcTimestamp);
        return jsonObject.toString();
    }

    public static Create fromString(String from) throws GeneralSecurityException, IOException {
        JSONObject jsonObject = new JSONObject(from);
        return new Create(
                KeyDeserializer.forBytes(
                        Hex.decode(
                                jsonObject.getString("key")
                        )
                ).toPublicKey(),
                jsonObject.getLong("timestamp")
        );
    }

    public long getUTCTimestampInSeconds(){
        return utcTimestamp;
    }

    public byte[] toBytes(){
        return new BlockBytesFromStringFactory(toString()).getBytes();
    }

    public byte[] getPreviousReference(){
        return null;
    }

    public boolean isGenesis(){
        return true;
    }

    public String getType(){
        return "create";
    }

    public String getAccountType(){
        return getAccountTypeStatic();
    }

    public ImmutableMap<String, Object> getInitializationParams(){
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        builder.put("publicKey", publicKey);
        return builder.build();
    }

    public static String getAccountTypeStatic() {
        return "basic_tink";
    }

    public static Create forKey(PublicKey key){
        return new Create(key);
    }

    public static Create forKeyWithTimestamp(PublicKey key, long timestamp){
        return new Create(key, timestamp);
    }

}
