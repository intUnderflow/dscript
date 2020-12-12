package site.intunderflow.dscript.utility;

import com.google.common.base.Preconditions;

import java.util.List;

public class ListProvider<T> implements Provider<T> {

    private final List<T> list;

    public ListProvider(
            List<T> list
    ){
        this.list = Preconditions.checkNotNull(list);
    }

    @Override
    public boolean exhausted() {
        return list.size() == 0;
    }

    @Override
    public T provide(){
        T value = list.get(0);
        list.remove(0);
        return value;
    }
}
