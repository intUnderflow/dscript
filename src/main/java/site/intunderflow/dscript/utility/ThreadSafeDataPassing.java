package site.intunderflow.dscript.utility;

import com.google.common.base.Preconditions;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ThreadSafeDataPassing<T> {

    private static final Logger logger = Logger.getLogger(ThreadSafeDataPassing.class.getSimpleName());

    private final BlockingQueue<T> queue;

    public ThreadSafeDataPassing(){
        queue = new LinkedBlockingDeque<>();
    }

    public void pass(T value){
        Preconditions.checkState(queue.isEmpty(), "Value has already been passed.");
        try{
            queue.put(value);
        }
        catch(InterruptedException e){
            logger.log(Level.WARNING, e.getMessage());
        }
    }

    public T getPassed(long timeoutinSeconds){
        try{
            return queue.poll(timeoutinSeconds, TimeUnit.SECONDS);
        }
        catch(InterruptedException e){
            logger.log(Level.WARNING, e.getMessage());
            return null;
        }
    }

}
