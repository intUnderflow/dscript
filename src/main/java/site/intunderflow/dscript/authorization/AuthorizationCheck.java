package site.intunderflow.dscript.authorization;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.application.blocklattice.blockchain.BlockBytesFromStringFactory;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.CreateBlock;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.basic_tink.Create;
import site.intunderflow.dscript.utility.crypto.keys.PublicKey;

import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

public class AuthorizationCheck {

    private final Map<String, Object> initializationParams;

    public AuthorizationCheck(){
        this(new HashMap<>());
    }

    public AuthorizationCheck(Map<String, Object> initializationParams){
        this.initializationParams = Preconditions.checkNotNull(initializationParams);
    }

    public void checkAuthorization(String authorizationType, byte[] authorization, byte[] subject)
            throws GeneralSecurityException{
        switch(authorizationType){
            case "signature":
                verifySignature(authorization, subject);
                break;
            default:
                throw new GeneralSecurityException("Cannot verify authorization type " + authorizationType +
                        " because unsupported.");
        }
    }

    private void verifySignature(byte[] signature, byte[] subject) throws GeneralSecurityException {
        if (!initializationParams.containsKey("publicKey")){
            throw new GeneralSecurityException("No public key, cannot check signature!");
        }
        PublicKey publicKey = (PublicKey) initializationParams.get("publicKey");
        publicKey.verifySignatureViaExceptionThrow(signature, subject);
    }

}
