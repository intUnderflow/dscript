package site.intunderflow.dscript.utility.crypto.keys;

import com.google.common.base.Preconditions;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.proto.KeyTemplate;
import com.google.crypto.tink.signature.SignatureKeyTemplates;

import java.security.GeneralSecurityException;

public class KeyPair extends KeysetContainer {

    private final KeysetHandle keysetHandle;

    KeyPair(KeysetHandle keysetHandle){
        this.keysetHandle = Preconditions.checkNotNull(keysetHandle);
    }

    @Override
    KeysetHandle getKeysetHandle(){
        return keysetHandle;
    }

    public static KeyPair generateNew() throws GeneralSecurityException {
        return generateNew(SignatureKeyTemplates.ECDSA_P256);
    }

    public static KeyPair generateNewStrong() throws GeneralSecurityException {
        return generateNew(SignatureKeyTemplates.ECDSA_P384);
    }

    public static KeyPair generateNewExtraStrong() throws GeneralSecurityException {
        return generateNew(SignatureKeyTemplates.ECDSA_P521);
    }

    private static KeyPair generateNew(KeyTemplate keyTemplate) throws GeneralSecurityException{
        return new KeyPair(
                KeysetHandle.generateNew(
                        keyTemplate
                )
        );
    }

    public PublicKey getPublicKey() throws GeneralSecurityException {
        return new PublicKey(
                keysetHandle.getPublicKeysetHandle()
        );
    }

    public PrivateKey getPrivateKey() throws GeneralSecurityException {
        return new PrivateKey(
                keysetHandle
        );
    }
}
