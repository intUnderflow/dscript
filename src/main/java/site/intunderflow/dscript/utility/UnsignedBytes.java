package site.intunderflow.dscript.utility;

public class UnsignedBytes {

    /**
     * Gets the UNSIGNED value of the byte.
     * @param b - the byte to get the unsigned value of.
     * @return the unsigned value of the byte.
     */
    public static double getValue(byte b){
        if (b == 0){ return 0; }
        double value = b;
        boolean addOne;
        if (b < 0){
            addOne = true;
            value = value + 128;
        }
        else{
            addOne = false;
        }
        value = value * 2;
        if (addOne){
            value = value + 1;
        }
        return value;
    }

}
