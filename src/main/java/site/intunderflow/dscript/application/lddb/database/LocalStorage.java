package site.intunderflow.dscript.application.lddb.database;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.application.lddb.resource.Identifier;
import site.intunderflow.dscript.network.message.content.LDDBBroadcast;
import site.intunderflow.dscript.utility.ConfReader;
import site.intunderflow.dscript.utility.Hex;
import site.intunderflow.dscript.utility.Resources;

import java.io.*;
import java.nio.Buffer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Local Storage of items for an LDDB Database.
 * A map of keys to values.
 */
public class LocalStorage {

    private static final Logger logger = Logger.getLogger(LocalStorage.class.getName());

    private final File file;

    private final Map<Identifier, byte[]> map;

    public LocalStorage(
            String fileName
    ){
        this.file = new File(Preconditions.checkNotNull(fileName));
        if (file.exists()){
            map = attemptLoadMap();
        }
        else{
            map = new HashMap<>();
            put(Resources.getGenesisBlock());
        }
    }

    public byte[] get(Identifier identifier){
        return map.getOrDefault(identifier, new byte[0]);
    }

    public Identifier put(byte[] data){
        Identifier identifier = Identifier.forData(data);
        put(identifier, data);
        return identifier;
    }

    void put(Identifier identifier, byte[] data){
        map.put(identifier, data);
        attemptWriteFile();
    }

    private Map<Identifier, byte[]> attemptLoadMap(){
        Map<Identifier, byte[]> loadedMap = new HashMap<>();
        ConfReader.readConfigurationFile(
                file,
                (index, value) -> {
                    Identifier identifier = Identifier.fromString(
                            Hex.decodeString(index)
                    );
                    byte[] bytes = Hex.decode(value);
                    loadedMap.put(identifier, bytes);
                }
        );
        return loadedMap;
    }

    boolean isEmpty(){
        return map.isEmpty();
    }

    public int size(){
        return map.size();
    }

    private String encodeLine(Identifier identifier, byte[] value){
        return Hex.encodeString(identifier.toString()) + "=" + Hex.encode(value);
    }

    private void attemptWriteFile(){
        try{
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            boolean firstWrite = true;
            for(Map.Entry<Identifier, byte[]> entry : map.entrySet()){
                if (!firstWrite){
                    bufferedWriter.write("\r\n");
                }
                bufferedWriter.write(encodeLine(entry.getKey(), entry.getValue()));
                firstWrite = false;
            }
            bufferedWriter.close();
        }
        catch(Exception e){
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

}
