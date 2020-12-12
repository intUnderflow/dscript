package site.intunderflow.dscript.utility;

public interface Provider<T> {

    T provide();

    boolean exhausted();

}
