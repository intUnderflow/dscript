package site.intunderflow.dscript.utility;

public interface Filter<T> {

    boolean shouldFilter(T object);

}
