package site.intunderflow.dscript.utility;

import com.google.common.base.Preconditions;

public class ByteArrayReader {

    private final byte[] data;

    private int pointer;

    public ByteArrayReader(byte[] data){
        this.data = Preconditions.checkNotNull(data);
        this.pointer = 0;
    }

    public int size(){
        return data.length;
    }

    public byte nextByte(){
        return readByte();
    }

    public byte[] nextBytes(int amount){
        byte[] array = new byte[amount];
        for (int i = 0; i < amount; i++){
            array[i] = readByte();
        }
        return array;
    }

    public byte peekByte(){
        byte toReturn = readByte();
        pointer--;
        return toReturn;
    }

    public byte[] peekBytes(int amount){
        byte[] toReturn = nextBytes(amount);
        pointer = pointer - amount;
        return toReturn;
    }

    private byte readByte(){
        byte nextByte = data[pointer];
        pointer++;
        return nextByte;
    }

}
