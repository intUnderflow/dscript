package site.intunderflow.dscript.utility;

import com.google.common.math.DoubleMath;

import java.util.Arrays;
import java.util.BitSet;

public class ByteArrayCreator {

    public static byte[] fromInt(int from){
        return fromLong(from);
    }

    public static byte[] fromLong(long from){
        if (from == 0){
            return new byte[0];
        }
        int bitsRequired = getBitsRequiredForNumber(from);
        boolean[] bits = new boolean[bitsRequired];
        for (int i = bitsRequired - 1; i >= 0; i--){
            long value = (long)Math.pow(2, i);
            if (value <= from){
                from = from - value;
                bits[i] = Bit.ONE;
            }
            else{
                bits[i] = Bit.ZERO;
            }
        }
        byte[] bytes = new byte[(int)(Math.ceil((double)bits.length / 8))];
        int currentByteIndex = 0;
        boolean[] currentByte = new boolean[8];
        int counter = 0;
        for (int i = 0; i < bits.length; i++){
            currentByte[counter] = bits[i];
            counter++;
            if (counter == 8){
                // Turn to byte.
                byte madeByte = getByteUnsigned(currentByte);
                currentByte = new boolean[8];
                bytes[currentByteIndex] = madeByte;
                currentByteIndex++;
                counter = 0;
            }
        }
        if (counter > 0){
            // Turn to byte.
            byte madeByte = getByteUnsigned(currentByte);
            bytes[currentByteIndex] = madeByte;
        }
        return bytes;
    }

    private static byte getByteUnsigned(boolean[] bits){
        // Get the signed value.
        double value = 0;
        int position = 0;
        for (boolean bit : bits){
            if (bit){
                if (position == 0){
                    value = value - 128;
                }
                else{
                    value = value + Math.pow(2, position - 1);
                }
            }
            position++;
        }
        return (byte) value;
    }

    private static int getBitsRequiredForNumber(long number){
        int count = 0;
        while (number > 0) {
            count++;
            number = number >> 1;
        }
        return count;
    }

}
