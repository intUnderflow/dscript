package site.intunderflow.dscript.utility;

import org.json.JSONObject;
import site.intunderflow.dscript.application.executor.ddl.v1.memory.ByteValue64;

import java.util.HashMap;
import java.util.Map;

public class JSONUtils {

    public static Map<String, String> jsonToFiles(JSONObject jsonObject){
        Map<String, String> files = new HashMap<>();
        for (String index : jsonObject.keySet()){
            String value = jsonObject.getString(
                    index
            );
            files.put(index, value);
        }
        return files;
    }

    public static JSONObject filesToJSON(Map<String, String> files){
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<String, String> entry : files.entrySet()){
            jsonObject.put(
                    entry.getKey(),
                    entry.getValue()
            );
        }
        return jsonObject;
    }

    public static JSONObject permanentMemoryToJSON(Map<ByteValue64, ByteValue64> memory){
        JSONObject json = new JSONObject();
        for (Map.Entry<ByteValue64, ByteValue64> entry : memory.entrySet()){
            json.put(
                    Hex.encode(entry.getKey().getValue().getArray()),
                    Hex.encode(entry.getValue().getValue().getArray())
            );
        }
        return json;
    }

    public static Map<ByteValue64, ByteValue64> jsonToPermanentMemory(JSONObject jsonObject){
        Map<ByteValue64, ByteValue64> memory = new HashMap<>();
        for (String strIndex : jsonObject.keySet()){
            ByteValue64 index = new ByteValue64(
                    Hex.decode(
                            strIndex
                    )
            );
            ByteValue64 value = new ByteValue64(
                    Hex.decode(
                            jsonObject.getString(
                                    strIndex
                            )
                    )
            );
            memory.put(index, value);
        }
        return memory;
    }

}
