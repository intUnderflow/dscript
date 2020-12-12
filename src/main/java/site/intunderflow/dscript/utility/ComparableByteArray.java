package site.intunderflow.dscript.utility;

import java.util.Arrays;

/**
 * Byte arrays aren't comparable or hashable.
 * This class wraps them in a class that is.w
 */
public class ComparableByteArray {

    private final byte[] array;

    public ComparableByteArray(byte[] array){
        if (array == null){
            this.array = new byte[0];
        }
        else {
            this.array = array;
        }
    }

    public byte[] getArray(){
        return array;
    }

    @Override
    public int hashCode(){
        return Arrays.hashCode(array);
    }

    @Override
    public boolean equals(Object o){
        if (o == null){
            return false;
        }
        else if (o.getClass().equals(getClass())){
            ComparableByteArray compare = (ComparableByteArray) o;
            return Arrays.equals(array, compare.getArray());
        }
        else{
            return false;
        }
    }

}
