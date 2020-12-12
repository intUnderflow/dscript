package site.intunderflow.dscript.utility.crypto.prng;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Class used for Psuedorandom Number Generation.
 */
public class PRNG {

    private static final String PRNG_IMPLEMENTATION = "SHA1PRNG";

    private final SecureRandom secureRandom;

    public PRNG(byte[] seed){
        try {
            this.secureRandom = SecureRandom.getInstance(PRNG_IMPLEMENTATION);
        }
        catch(NoSuchAlgorithmException e){
            throw new Error(e);
        }
        secureRandom.setSeed(seed);
    }

    public long nextLong(){
        return secureRandom.nextLong();

    }


}
