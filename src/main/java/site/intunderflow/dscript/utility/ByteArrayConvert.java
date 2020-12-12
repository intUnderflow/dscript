package site.intunderflow.dscript.utility;

public class ByteArrayConvert {

    public static long toLong(byte[] array){
        long total = 0;
        int i = 0;
        for (boolean bit : new ByteArrayBitIterable(array)){
            if (bit){
                total = total + (long) Math.pow(2, i);
            }
            i++;
        }
        return total;
    }

    public static int toInt(byte[] array){
        return (int) toLong(array);
    }

}
