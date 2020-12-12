package site.intunderflow.dscript.application.lddb.resource;

import com.google.common.base.Preconditions;
import org.json.JSONObject;
import site.intunderflow.dscript.utility.Hex;
import site.intunderflow.dscript.utility.hashing.SHA512;

import java.util.Arrays;

public class Identifier {

    private static final int CREATE_AS_VERSION = 0;

    private final int version;

    private final byte[] hash;

    private Identifier(
            int version,
            byte[] hash
    ){
        this.version = version;
        this.hash = Preconditions.checkNotNull(hash);
    }

    public Identifier(
            byte[] hash
    ){
        this(CREATE_AS_VERSION, hash);
    }

    public String toString(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("version", version);
        jsonObject.put("hash", Hex.encode(hash));
        return jsonObject.toString();
    }

    public static Identifier fromString(String identifier){
        JSONObject jsonObject = new JSONObject(identifier);
        return new Identifier(
                jsonObject.getInt("version"),
                Hex.decode(jsonObject.getString("hash"))
        );
    }

    public static Identifier forData(byte[] data){
        byte[] hash = SHA512.hash(data);
        return new Identifier(hash);
    }

    public static Identifier forHash(byte[] hash){
        return new Identifier(hash);
    }

    public boolean isForData(byte[] data){
        return hashMatches(data);
    }

    private boolean hashMatches(byte[] data){
        return Arrays.equals(
                hash,
                SHA512.hash(data)
        );
    }

    public byte[] getHash(){
        return hash;
    }

    @Override
    public int hashCode(){
        return Arrays.hashCode(hash);
    }

    @Override
    public boolean equals(Object o){
        if (o == null){
            return false;
        }
        else if (getClass() != o.getClass()){
            return false;
        }
        else{
            Identifier forComparison = (Identifier) o;
            return Arrays.equals(
                    forComparison.getHash(),
                    getHash()
            );
        }
    }

}
