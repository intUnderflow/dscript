package site.intunderflow.dscript.utility;

import java.util.ArrayList;
import java.util.List;

public class ByteArrayWriter {

    private final List<Byte> bytes;

    public ByteArrayWriter(){
        bytes = new ArrayList<>();
    }

    public byte[] getBytes(){
        byte[] array = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++){
            array[i] = bytes.get(i);
        }
        return array;
    }

    public void writeByte(byte value){
        bytes.add(value);
    }

    public void writeBytes(byte[] array){
        for (byte value : array){
            writeByte(value);
        }
    }

}
