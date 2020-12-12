package site.intunderflow.dscript.utility;

public class DualBuilder<A, B> {

    private A a;

    private B b;

    public DualBuilder setA(A a){
        this.a = a;
        return this;
    }

    public DualBuilder setB(B b){
        this.b = b;
        return this;
    }

    public Dual<A, B> build(){
        return new BuiltDual(a, b);
    }

    private class BuiltDual implements Dual<A, B>{

        private final A a;

        private final B b;

        private BuiltDual(A a, B b){
            this.a = a;
            this.b = b;
        }

        @Override
        public A getA() {
            return a;
        }

        @Override
        public B getB() {
            return b;
        }
    }

}
