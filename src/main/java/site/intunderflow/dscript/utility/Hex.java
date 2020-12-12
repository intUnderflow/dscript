package site.intunderflow.dscript.utility;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Hex {

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    public static String encode(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    public static byte[] decode(String hex){
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }

    public static String encodeString(String toEncode){
        return encode(toEncode.getBytes(CHARSET));
    }

    public static String decodeString(String hex){
        return new String(decode(hex), CHARSET);
    }

}
