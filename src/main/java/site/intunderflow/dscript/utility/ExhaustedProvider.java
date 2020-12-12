package site.intunderflow.dscript.utility;

public class ExhaustedProvider<T> implements Provider<T> {

    @Override
    public T provide(){
        return null;
    }

    @Override
    public boolean exhausted(){
        return true;
    }

}
