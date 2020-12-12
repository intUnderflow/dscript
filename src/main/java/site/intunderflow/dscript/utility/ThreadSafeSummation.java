package site.intunderflow.dscript.utility;

import com.google.common.base.Preconditions;

import java.util.Stack;

public class ThreadSafeSummation {

    private final Stack<Long> stack;

    private final Flag dead;

    public ThreadSafeSummation(){
        stack = new Stack<>();
        dead = new Flag();
    }

    private void checkState(){
        Preconditions.checkState(!dead.isRaised(), "Cannot use class once summation done.");
    }

    public void add(Long value){
        checkState();
        stack.push(value);
    }

    /** Class is dead once this is called. */
    public Long sum(){
        checkState();
        dead.raise();
        long total = 0;
        while (!stack.empty()){
            total = total + stack.pop();
        }
        return total;
    }

    public Long peekSum(){
        checkState();
        long total = 0;
        for (Long current : stack){
            total = total + current;
        }
        return total;
    }

}
