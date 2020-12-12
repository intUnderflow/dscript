package site.intunderflow.dscript.utility;

public class Flag {

    private volatile boolean flag;

    public Flag(){
        this.flag = false;
    }

    public synchronized void raise(){
        this.flag = true;
    }

    public synchronized boolean isRaised(){
        return this.flag;
    }

}
