package site.intunderflow.dscript.utility.crypto.keys;

import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.KeysetHandle;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class KeyDeserializer {

    private final KeysetHandle keysetHandle;

    private KeyDeserializer(byte[] bytes) throws GeneralSecurityException, IOException {
        this.keysetHandle = CleartextKeysetHandle.read(
                JsonKeysetReader.withBytes(bytes)
        );
    }

    private KeyDeserializer(File file) throws GeneralSecurityException, IOException {
        this.keysetHandle = CleartextKeysetHandle.read(
                JsonKeysetReader.withFile(file)
        );
    }

    public KeyPair toKeyPair(){
        return new KeyPair(keysetHandle);
    }

    public PublicKey toPublicKey(){
        return new PublicKey(keysetHandle);
    }

    public PrivateKey toPrivateKey(){
        return new PrivateKey(keysetHandle);
    }

    public static KeyDeserializer forBytes(byte[] bytes) throws GeneralSecurityException, IOException {
        return new KeyDeserializer(bytes);
    }

    public static KeyDeserializer forFile(File file) throws GeneralSecurityException, IOException {
        return new KeyDeserializer(file);
    }

}
