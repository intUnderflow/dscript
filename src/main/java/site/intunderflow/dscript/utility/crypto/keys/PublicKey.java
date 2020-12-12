package site.intunderflow.dscript.utility.crypto.keys;

import com.google.common.base.Preconditions;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.signature.PublicKeyVerifyFactory;
import com.google.errorprone.annotations.CheckReturnValue;
import site.intunderflow.dscript.utility.Hex;

import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PublicKey extends KeysetContainer {

    private static final Logger logger = Logger.getLogger(PublicKey.class.getName());

    private final KeysetHandle keysetHandle;

    PublicKey(KeysetHandle keysetHandle){
        this.keysetHandle = Preconditions.checkNotNull(keysetHandle);
    }

    @Override
    KeysetHandle getKeysetHandle() {
        return keysetHandle;
    }

    @CheckReturnValue
    public boolean verifySignature(byte[] signature, byte[] content) {
        System.out.println("VERIFYING " + Hex.encode(content));
        try{
            verifySignatureViaExceptionThrow(signature, content);
            return true;
        }
        catch(GeneralSecurityException e){
            logger.log(Level.INFO, e.getMessage());
            return false;
        }
    }

    public void verifySignatureViaExceptionThrow(byte[] signature, byte[] content) throws GeneralSecurityException {
        PublicKeyVerifyFactory.getPrimitive(keysetHandle).verify(signature, content);
    }

}
