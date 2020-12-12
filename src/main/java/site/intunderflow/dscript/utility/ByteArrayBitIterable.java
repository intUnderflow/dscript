package site.intunderflow.dscript.utility;

import java.util.Iterator;

import static site.intunderflow.dscript.utility.UnsignedBytes.getValue;

// Credit: https://stackoverflow.com/questions/1034473/java-iterate-bits-in-byte-array
public class ByteArrayBitIterable implements Iterable<Boolean> {
    private final byte[] array;

    public ByteArrayBitIterable(byte[] array) {
        this.array = array;
    }

    public Iterator<Boolean> iterator() {
        if (array.length > 0){
            return new BitIterator();
        }
        else{
            return new EmptyIterator();
        }
    }

    private class EmptyIterator implements Iterator<Boolean> {

        @Override
        public Boolean next(){
            return null;
        }

        @Override
        public boolean hasNext(){
            return false;
        }

    }

    private class BitIterator implements Iterator<Boolean> {

        private boolean[] getBits(byte b){
            double amount = getValue(b);
            boolean[] bits = new boolean[8];
            for (int i = 7; i >= 0; i--){
                double value = Math.pow(2, i);
                if (amount >= value){
                    amount = amount - value;
                    bits[i] = Bit.ONE;
                }
                else{
                    bits[i] = Bit.ZERO;
                }
            }
            return bits;
        }

        boolean[] bits;

        int bitIndex;

        int byteIndex;

        public BitIterator(){
            byteIndex = 0;
            getNextByte();
        }

        private void getNextByte(){
            bits = getBits(array[byteIndex]);
            byteIndex++;
            bitIndex = 0;
        }

        private boolean requiresNextByte(){
            return bitIndex == 8;
        }

        private boolean nextByteAvailable(){
            return byteIndex < array.length;
        }

        @Override
        public boolean hasNext() {
            return !(requiresNextByte() && !nextByteAvailable());
        }

        @Override
        public Boolean next() {
            if (requiresNextByte()){
                getNextByte();
            }
            boolean bit = bits[bitIndex];
            bitIndex++;
            return bit;
        }

    }

}
