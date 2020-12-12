package site.intunderflow.dscript.runners;

import site.intunderflow.dscript.utility.BitString;
import site.intunderflow.dscript.utility.ByteArrayBitIterable;

import java.util.BitSet;

public class ByteTest {

    public static void main(String[] args){
        byte[] a = new byte[2];
        a[0] = 7;
        a[1] = 3;
        boolean[] bits = new boolean[16];
        int i = 0;
        for (boolean bit : new ByteArrayBitIterable(a)){
            bits[i] = bit;
            i++;
        }
        System.out.println(BitString.toString(bits));
    }

}
