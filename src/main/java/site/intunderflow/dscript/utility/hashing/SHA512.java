package site.intunderflow.dscript.utility.hashing;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA512 implements Hasher {

    private static final String HASH_ALGORITHM = "SHA-512";

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    public static byte[] hash(byte[] content) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(HASH_ALGORITHM);
            messageDigest.update(content);
            return messageDigest.digest();
        }
        catch(NoSuchAlgorithmException e) {
            throw new Error(e);
        }
    }

    public static byte[] hash(String content){
        return hash(content.getBytes(CHARSET));
    }
}
