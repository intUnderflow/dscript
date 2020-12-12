package site.intunderflow.dscript.utility;

public class BitString {

    public static String toString(byte v){
        byte[] bytes = new byte[1];
        bytes[0] = v;
        return toString(bytes);
    }

    public static String toString(boolean bit){
        if (bit){
            return "1";
        }
        else{
            return "0";
        }
    }

    public static String toString(boolean[] bits){
        StringBuilder builder = new StringBuilder();
        for (boolean bit : bits){
            if (bit){
                builder.append(1);
            }
            else{
                builder.append(0);
            }
        }
        return builder.toString();
    }

    public static String toString(byte[] bytes){
        StringBuilder builder = new StringBuilder();
        for (boolean bit : new ByteArrayBitIterable(bytes)){
            if (bit){
                builder.append(1);
            }
            else{
                builder.append(0);
            }
        }
        return builder.toString();
    }

    public static String toStringWithSpaces(boolean[] bits){
        String withoutSpaces = toString(bits);
        return withSpaces(withoutSpaces);
    }

    public static String toStringWithSpaces(byte[] bytes){
        String withoutSpaces = toString(bytes);
        return withSpaces(withoutSpaces);
    }

    private static String withSpaces(String withoutSpaces){
        StringBuilder builder = new StringBuilder();
        int position = 1;
        for (int i = 0; i < withoutSpaces.length(); i++){
            char c = withoutSpaces.charAt(i);
            builder.append(c);
            if (position == 8){
                builder.append(" ");
                position = 1;
            }
            else{
                position++;
            }
        }
        return builder.toString();
    }

}
