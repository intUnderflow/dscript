package site.intunderflow.dscript.utility;

import java.util.*;

/**
 * Items can be pushed to the conveyor and you can check if an item is on the conveyor.
 * Once the maximum size is reached the oldest items are removed.
 */
public class FixedLengthConveyor<T> {

    private final int size;

    private final Queue<T> objects;

    public FixedLengthConveyor(int size){
        this.size = size;
        this.objects = new LinkedList<>();
    }

    public void add(T object){
        objects.add(object);
        if (objects.size() > size){
            objects.poll();
        }
    }

    public boolean contains(T object){
        return objects.contains(object);
    }

    public List<T> getAll(){
        List<T> list = new ArrayList<>();
        objects.forEach(list::add);
        return list;
    }

}
