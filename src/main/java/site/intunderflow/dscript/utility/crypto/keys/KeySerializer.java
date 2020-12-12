package site.intunderflow.dscript.utility.crypto.keys;

import com.google.common.base.Preconditions;
import com.google.crypto.tink.BinaryKeysetReader;
import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetWriter;
import com.google.crypto.tink.KeysetHandle;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class KeySerializer {

    private final KeysetHandle keysetHandle;

    public KeySerializer(KeysetContainer container){
        this(Preconditions.checkNotNull(container).getKeysetHandle());
    }

    public KeySerializer(KeysetHandle handle){
        this.keysetHandle = Preconditions.checkNotNull(handle);
    }

    public byte[] serialize(){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try{
            CleartextKeysetHandle.write(keysetHandle, JsonKeysetWriter.withOutputStream(outputStream));
        }
        catch(IOException e){
            return new byte[0];
        }
        return outputStream.toByteArray();
    }

    public void serializeToFile(File file) throws IOException {
        CleartextKeysetHandle.write(
                keysetHandle,
                JsonKeysetWriter.withFile(file)
        );
    }

    public static KeySerializer forPublicKey(PublicKey publicKey){
        return new KeySerializer(publicKey.getKeysetHandle());
    }

    public static KeySerializer forPrivateKey(PrivateKey privateKey){
        return new KeySerializer(privateKey.getKeysetHandle());
    }

}
