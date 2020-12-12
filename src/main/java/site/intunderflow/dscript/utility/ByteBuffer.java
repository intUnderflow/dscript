package site.intunderflow.dscript.utility;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

public class ByteBuffer {

    private final byte[] bytes;

    public ByteBuffer(int bytesAllocated){
        bytes = new byte[bytesAllocated];
    }

    private ByteBuffer(byte[] value){
        bytes = Preconditions.checkNotNull(value);
    }

    @CanIgnoreReturnValue
    public ByteBuffer put(byte toPut){
        bytes[0] = toPut;
        return this;
    }

    @CanIgnoreReturnValue
    public ByteBuffer put(byte[] toPut){
        System.arraycopy(
                toPut,
                0,
                bytes,
                0,
                toPut.length
        );
        return this;
    }

    @CanIgnoreReturnValue
    public ByteBuffer putInt(int toPut){
        if (toPut != 0) {
            put(ByteArrayCreator.fromInt(toPut));
        }
        return this;
    }

    @CanIgnoreReturnValue
    public ByteBuffer putLong(long toPut){
        if (toPut != 0) {
            put(ByteArrayCreator.fromLong(toPut));
        }
        return this;
    }

    public byte[] array(){
        return bytes;
    }

    public byte get(){
        return bytes[0];
    }

    public int getInt(){
        return ByteArrayConvert.toInt(bytes);
    }

    public long getLong(){
        return ByteArrayConvert.toLong(bytes);
    }

    public void get(byte[] putTo){
        System.arraycopy(
                bytes,
                0,
                putTo,
                0,
                putTo.length
        );
    }

    public static ByteBuffer allocate(int amount){
        return new ByteBuffer(amount);
    }

    public static ByteBuffer wrap(byte[] toWrap){
        return new ByteBuffer(toWrap);
    }

}
