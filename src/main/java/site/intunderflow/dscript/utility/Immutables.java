package site.intunderflow.dscript.utility;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.List;

public class Immutables<T> {

    public List<T> cloneToMutableList(ImmutableList<T> immutableList) {
        List<T> list = new ArrayList<>();
        for (T entry : immutableList) {
            list.add(entry);
        }
        return list;
    }

}
