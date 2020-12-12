package site.intunderflow.dscript.application.consensus.dpos;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.application.blocklattice.blockchain.address.BaseAddress;
import site.intunderflow.dscript.utility.Hex;
import site.intunderflow.dscript.utility.crypto.keys.KeyPair;
import site.intunderflow.dscript.utility.crypto.keys.PrivateKey;

import java.security.GeneralSecurityException;

public class SignatureFactory {

    private final PrivateKey privateKey;

    private final BaseAddress baseAddress;

    public SignatureFactory(KeyPair keyPair, BaseAddress baseAddress){
        try{
            this.privateKey = Preconditions.checkNotNull(keyPair.getPrivateKey());
        }
        catch(GeneralSecurityException e){
            throw new Error(e);
        }
        this.baseAddress = Preconditions.checkNotNull(baseAddress);
    }

    @Deprecated
    public SignatureFactory(PrivateKey privateKey, BaseAddress baseAddress){
        this.privateKey = Preconditions.checkNotNull(privateKey);
        this.baseAddress = Preconditions.checkNotNull(baseAddress);
    }

    public BaseAddress getBaseAddress() {
        return baseAddress;
    }

    public Signature sign(byte[] content) throws GeneralSecurityException {
        System.out.println("SIGNING " + Hex.encode(content));
        return new Signature(
                baseAddress,
                privateKey.sign(content)
        );
    }

}
