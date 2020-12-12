package site.intunderflow.dscript.application.multipartypinseeding;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.application.consensus.dpos.Signature;

/**
 * Wrapper of {@link Signature} in case of extension of MPPS Signatures in future.
 */
public class MPPSSignature {

   private final Signature signature;

   public MPPSSignature(Signature signature){
       this.signature = Preconditions.checkNotNull(signature);
   }

    public Signature getSignature() {
        return signature;
    }

    @Override
    public String toString(){
       return signature.toString();
    }

    public static MPPSSignature fromString(String from){
       return new MPPSSignature(
               Signature.fromString(from)
       );
    }


}
