package site.intunderflow.dscript.utility;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Base64 {

    private static final java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();

    private static final java.util.Base64.Decoder decoder = java.util.Base64.getDecoder();

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    public static byte[] encode (byte[] toEncode){
        return encoder.encode(toEncode);
    }

    public static byte[] decode(byte[] toDecode){
        return decoder.decode(toDecode);
    }

    public static String encode (String toEncode){
        return new String(encode(toEncode.getBytes(CHARSET)), CHARSET);
    }

    public static String decode (String toDecode){
        return new String(decode(toDecode.getBytes(CHARSET)), CHARSET);
    }

}
