package site.intunderflow.dscript.application.blocklattice.blockchain.block.dapp_ddl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import site.intunderflow.dscript.application.blocklattice.blockchain.BlockBytesFromStringFactory;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.DAppStateChangeBlock;
import site.intunderflow.dscript.application.consensus.dpos.Signature;
import site.intunderflow.dscript.application.executor.ddl.v1.memory.ByteValue64;
import site.intunderflow.dscript.utility.Hex;
import site.intunderflow.dscript.utility.JSONUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StateChanged extends DAppStateChangeBlock {

    private final ImmutableList<Signature> authoritySignatures;

    private final ImmutableMap<ByteValue64, ByteValue64> permanentMemory;

    private final long utcTimestamp;

    private final byte[] previous;

    @Override
    public byte[] getContentToSign(){
        return ArrayUtils.addAll(
                previous,
                new BlockBytesFromStringFactory(
                        JSONUtils.permanentMemoryToJSON(
                                permanentMemory
                        ).toString()
                ).getBytes()
        );
    }

    public long getUTCTimestampInSeconds(){
        return utcTimestamp;
    }

    public byte[] getPreviousReference(){
        return previous;
    }

    public boolean isGenesis(){
        return false;
    }

    public String getType(){
        return "dapp_state_changed";
    }

    public static String getAccountTypeStatic() {
        return "dapp_ddl";
    }

    public String getAccountType(){
        return getAccountTypeStatic();
    }

    public StateChanged(
            byte[] previous,
            List<Signature> authoritySignatures,
            Map<ByteValue64, ByteValue64> permanentMemory,
            long utcTimestamp
    ){
        this.previous = Preconditions.checkNotNull(previous);
        this.authoritySignatures = ImmutableList.copyOf(
                Preconditions.checkNotNull(
                        authoritySignatures
                )
        );
        this.permanentMemory = ImmutableMap.copyOf(
                Preconditions.checkNotNull(
                        permanentMemory
                )
        );
        this.utcTimestamp = utcTimestamp;
    }

    public byte[] toBytes(){
        return new BlockBytesFromStringFactory(toString()).getBytes();
    }

    @Override
    public String toString(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("account", getAccountType());
        jsonObject.put("type", getType());
        jsonObject.put("account", getAccountType());
        jsonObject.put("previous", Hex.encode(previous));
        jsonObject.put("timestamp", utcTimestamp);
        JSONArray authority = new JSONArray();
        for (Signature signature : authoritySignatures){
            authority.put(signature.toString());
        }
        jsonObject.put("authority", authority);
        JSONObject memory = JSONUtils.permanentMemoryToJSON(permanentMemory);
        jsonObject.put("memory", memory);
        return jsonObject.toString();
    }

    public static StateChanged fromString(String from){
        JSONObject jsonObject = new JSONObject(from);
        byte[] previous = Hex.decode(
                jsonObject.getString("previous")
        );
        long timestamp = jsonObject.getLong("timestamp");
        List<Signature> authority = new ArrayList<>();
        JSONArray authorityArray = jsonObject.getJSONArray("authority");
        for (int i = 0 ; i < authorityArray.length(); i++){
            String signature = authorityArray.getString(i);
            authority.add(Signature.fromString(signature));
        }
        Map<ByteValue64, ByteValue64> memory = JSONUtils.jsonToPermanentMemory(
                jsonObject.getJSONObject("memory")
        );
        return new StateChanged(
                previous,
                authority,
                memory,
                timestamp
        );
    }

    @Override
    public ImmutableList<Signature> getAuthoritySignatures(){
        return authoritySignatures;
    }

    @Override
    public ImmutableMap<ByteValue64, ByteValue64> getPermanentMemory(){
        return permanentMemory;
    }

}
