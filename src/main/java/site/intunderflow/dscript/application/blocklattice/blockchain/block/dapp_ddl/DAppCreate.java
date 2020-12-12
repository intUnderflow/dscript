package site.intunderflow.dscript.application.blocklattice.blockchain.block.dapp_ddl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.json.JSONObject;
import site.intunderflow.dscript.application.blocklattice.blockchain.BlockBytesFromStringFactory;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.CreateBlock;
import site.intunderflow.dscript.application.executor.ddl.v1.memory.ByteValue64;
import site.intunderflow.dscript.application.executor.ddl.v1.program.InstructionSet;
import site.intunderflow.dscript.utility.Hex;
import site.intunderflow.dscript.utility.JSONUtils;
import site.intunderflow.dscript.utility.Time;

import java.util.HashMap;
import java.util.Map;

public class DAppCreate extends CreateBlock {

    private final int version;

    private final InstructionSet instructionSet;

    private final Map<ByteValue64, ByteValue64> startPermanentMemory;

    private final Map<String, String> files;

    private final long utcTimestamp;

    public DAppCreate(
            InstructionSet sourcecode,
            Map<String, String> files
    ){
        this(sourcecode.getVersion(), files, sourcecode);
    }

    public DAppCreate(
            InstructionSet sourceCode
    ){
        this(sourceCode.getVersion(), sourceCode);
    }

    public DAppCreate(
            int version,
            InstructionSet sourceCode
    ){
        this(version, sourceCode, Time.getUTCTimestampInSeconds());
    }

    public DAppCreate(
            int version,
            InstructionSet sourceCode,
            long utcTimestamp
    ){
        this(version, sourceCode, new HashMap<>(), utcTimestamp);
    }

    public DAppCreate(
            int version,
            Map<String, String> files,
            InstructionSet sourceCode
    ){
        this(version, sourceCode, new HashMap<>(), files, Time.getUTCTimestampInSeconds());
    }

    public DAppCreate(
            int version,
            InstructionSet sourceCode,
            Map<ByteValue64, ByteValue64> permanentMemory
    ){
        this(version, sourceCode, permanentMemory, Time.getUTCTimestampInSeconds());
    }

    public DAppCreate(
            int version,
            InstructionSet sourceCode,
            Map<ByteValue64, ByteValue64> permanentMemory,
            long utcTimestamp
    ){
        this(version, sourceCode, permanentMemory, new HashMap<>(), utcTimestamp);
    }

    public DAppCreate(
            int version,
            InstructionSet sourceCode,
            Map<ByteValue64, ByteValue64> permanentMemory,
            Map<String, String> files,
            long utcTimestamp
    ){

        this.version = version;
        this.instructionSet = Preconditions.checkNotNull(sourceCode);
        this.startPermanentMemory = Preconditions.checkNotNull(permanentMemory);
        this.files = Preconditions.checkNotNull(files);
        this.utcTimestamp = utcTimestamp;
    }

    public static String getAccountTypeStatic() {
        return "dapp_ddl";
    }

    public String getAccountType(){
        return getAccountTypeStatic();
    }

    public ImmutableMap<String, Object> getInitializationParams(){
        return ImmutableMap.of(
                "permanentMemory", startPermanentMemory,
                "source", Hex.encode(instructionSet.toBytes()),
                "files", files
        );
    }

    public String getType(){
        return "dapp_create";
    }

    public static DAppCreate fromString(String from){
        JSONObject jsonObject = new JSONObject(from);
        long timestamp = jsonObject.getLong("timestamp");
        InstructionSet source = InstructionSet.fromString(
                jsonObject.getString("source")
        );
        int version = source.getVersion();
        Map<ByteValue64, ByteValue64> memory = JSONUtils.jsonToPermanentMemory(
                jsonObject.getJSONObject("permanentMemory")
        );
        Map<String, String> files = JSONUtils.jsonToFiles(
                jsonObject.getJSONObject("files")
        );
        return new DAppCreate(
                version,
                source,
                memory,
                files,
                timestamp
        );
    }

    @Override
    public String toString(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", getType());
        jsonObject.put("account", getAccountType());
        jsonObject.put("timestamp", utcTimestamp);
        jsonObject.put("source", instructionSet.toString());
        JSONObject memory = JSONUtils.permanentMemoryToJSON(startPermanentMemory);
        jsonObject.put("permanentMemory", memory);
        JSONObject filesJson = JSONUtils.filesToJSON(files);
        jsonObject.put("files", filesJson);
        return jsonObject.toString();
    }

    public int getVersion() {
        return version;
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

    @Override
    public long getUTCTimestampInSeconds() {
        return utcTimestamp;
    }

}
