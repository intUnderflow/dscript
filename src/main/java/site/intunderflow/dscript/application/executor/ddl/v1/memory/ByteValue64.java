package site.intunderflow.dscript.application.executor.ddl.v1.memory;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.utility.Bit;
import site.intunderflow.dscript.utility.ByteArrayBitIterable;
import site.intunderflow.dscript.utility.ComparableByteArray;

import site.intunderflow.dscript.utility.ByteBuffer;
import java.util.BitSet;

public class ByteValue64 {

    private final ComparableByteArray value;

    private final boolean overflowed;

    public ByteValue64(byte[] value){
        this(value, false);
    }

    public ByteValue64(byte[] value, boolean overflowed){
        checkInput(value);
        this.value = new ComparableByteArray(value);
        this.overflowed = overflowed;
    }

    private void checkInput(byte[] input){
        Preconditions.checkNotNull(input);
        Preconditions.checkArgument(input.length == 64, "Only 64 byte values are acceptable.");
    }

    public boolean overflowed() {
        return overflowed;
    }

    public ComparableByteArray getValue(){
        return value;
    }

    private static ByteValue64 newOfAll(byte value){
        byte[] array = new byte[64];
        for (int i = 0; i < 64; i++){
            array[i] = value;
        }
        return new ByteValue64(array);
    }

    public static ByteValue64 fromByte(byte byteFrom){
        byte[] array = new byte[64];
        array[0] = byteFrom;
        for (int i = 1; i < 64; i++){
            array[i] = 0x0;
        }
        return new ByteValue64(array);
    }

    public static ByteValue64 minimum(){
        return fromInteger(0);
    }

    public static ByteValue64 maximum(){
        return newOfAll(Byte.parseByte("11111111"));
    }

    public static ByteValue64 fromInteger(int value){
        byte[] array = ByteBuffer.allocate(64).putInt(value).array();
        return new ByteValue64(array);
    }

    private boolean[] toBits(byte[] bytes){
        boolean[] bits = new boolean[bytes.length * 8];
        int i = 0;
        for (boolean bit : new ByteArrayBitIterable(bytes)){
            bits[i] = bit;
            i++;
        }
        return bits;
    }

    /**
     * Adds this value to another {@link ByteValue64}. All values are treated as being unsigned. If there is an overflow
     * the .overflowed() method in the created {@link ByteValue64} will be true.
     * @param toAdd - The {@link ByteValue64 to add to this value.}
     * @return a new {@link ByteValue64} that represents the addition of this value and the provided value.
     */
    public ByteValue64 add(ByteValue64 toAdd){
        BitSet bitsForUs = BitSet.valueOf(value.getArray());
        BitSet bitsForThem = BitSet.valueOf(toAdd.getValue().getArray());
        BitSet summedBits = new BitSet();
        boolean carry = Bit.ZERO;
        for (int i = bitsForUs.size() - 1; i >= 0; i--){
            boolean ourBit = bitsForUs.get(i);
            boolean theirbit = bitsForThem.get(i);
            if (ourBit && theirbit){
                carry = Bit.ONE;
                summedBits.set(i, Bit.ZERO);
            }
            else if (carry){
                carry = Bit.ZERO;
                summedBits.set(i, Bit.ONE);
            }
            else if (ourBit || theirbit){
                summedBits.set(i, Bit.ONE);
            }
            else {
                summedBits.set(i, Bit.ZERO);
            }
        }
        return new ByteValue64(
                summedBits.toByteArray(),
                carry // Indicates overflow.
        );
    }

    @Override
    public int hashCode(){
        return value.hashCode();
    }

    @Override
    public boolean equals(Object o){
        if (o == null){
            return false;
        }
        else if (o.getClass().equals(getClass())){
            ByteValue64 toCompare = (ByteValue64) o;
            return value.equals(toCompare.value);
        }
        else{
            return false;
        }
    }

}
