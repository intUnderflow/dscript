package site.intunderflow.dscript.utility;

import com.google.common.base.Preconditions;

public class DualString implements Dual<String, String> {

    private final String a;

    private final String b;

    public DualString(
            String a,
            String b
    ){
        this.a = Preconditions.checkNotNull(a);
        this.b = Preconditions.checkNotNull(b);
    }

    @Override
    public String getA(){
        return a;
    }

    @Override
    public String getB(){
        return b;
    }

}
