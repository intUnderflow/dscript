package site.intunderflow.dscript.utility;

public class ArrayWriter<T> {

    private T[] array;

    private int pointer;

    public ArrayWriter(int length){
        @SuppressWarnings("unchecked")
        T[] arrayToMake = (T[]) new Object[length];
        this.array = arrayToMake;
        this.pointer = 0;
    }

    public void write(T item){
        array[pointer] = item;
        pointer++;
    }

    public T[] getArray(){
        return array;
    }

}
