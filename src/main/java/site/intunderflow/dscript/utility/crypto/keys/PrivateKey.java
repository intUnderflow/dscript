package site.intunderflow.dscript.utility.crypto.keys;

import com.google.common.base.Preconditions;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.PublicKeySign;
import com.google.crypto.tink.signature.PublicKeySignFactory;

import java.security.GeneralSecurityException;

public class PrivateKey extends KeysetContainer {

    private final KeysetHandle keysetHandle;

    public PrivateKey(
            KeysetHandle keysetHandle
    ){
        this.keysetHandle = Preconditions.checkNotNull(keysetHandle);
    }

    @Override
    KeysetHandle getKeysetHandle(){
        return keysetHandle;
    }

    public byte[] sign(byte[] content) throws GeneralSecurityException {
        PublicKeySign signer = PublicKeySignFactory.getPrimitive(keysetHandle);
        return signer.sign(content);
    }

}
