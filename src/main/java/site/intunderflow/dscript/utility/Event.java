package site.intunderflow.dscript.utility;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class Event<T> {

    private final ExecutorService executorService;

    private final List<Consumer<T>> consumers;

    private final List<Runnable> runnables;

    public Event(){
        executorService = Executors.newCachedThreadPool();
        consumers = new ArrayList<>();
        runnables = new ArrayList<>();
    }

    private void unsubscribe(Runnable runnable){ runnables.remove(runnable); }

    private void unsubscribe(Consumer<T> consumer){
        consumers.remove(consumer);
    }

    private void execute(Runnable runnable){
        executorService.execute(runnable);
    }

    public int getSubscriberCount(){
        return consumers.size();
    }

    @CanIgnoreReturnValue
    public Handle subscribeRunnable(Runnable runnable){
        runnables.add(runnable);
        return new RunnableEventHandle(
                this,
                runnable
        );
    }

    @CanIgnoreReturnValue
    public Handle subscribe(Consumer<T> consumer){
        consumers.add(consumer);
        return new ConsumerEventHandle(
                this,
                consumer
        );
    }

    public void fire(T object){
        for (Consumer<T> consumer : consumers){
            execute(
                    new RunningEventReaction<>(
                            consumer,
                            object
                    )
            );
        }
        for (Runnable runnable : runnables){
            execute(
                    new RunnableRunningEventReaction(
                            runnable
                    )
            );
        }
    }

    private class RunnableRunningEventReaction implements Runnable {

        private final Runnable runnable;

        private RunnableRunningEventReaction(Runnable runnable){
            this.runnable = Preconditions.checkNotNull(runnable);
        }

        @Override
        public void run(){
            try{
                runnable.run();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }

    }

    private class RunningEventReaction<T> implements Runnable {

        private final Consumer<T> consumer;

        private final T object;

        private RunningEventReaction(
                Consumer<T> consumer,
                T object
        ){
            this.consumer = Preconditions.checkNotNull(consumer);
            this.object = Preconditions.checkNotNull(object);
        }

        @Override
        public void run(){
            try{
                consumer.accept(object);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }

    }

    public interface Handle{
        void unsubscribe();
    }

    public class RunnableEventHandle implements Handle{

        private final Event<T> event;

        private final Runnable runnable;

        private RunnableEventHandle(
                Event<T> event,
                Runnable runnable
        ){
            this.event = Preconditions.checkNotNull(event);
            this.runnable = Preconditions.checkNotNull(runnable);
        }

        @Override
        public void unsubscribe(){
            event.unsubscribe(runnable);
        }

    }

    public class ConsumerEventHandle implements Handle {

        private final Event<T> event;

        private final Consumer<T> consumer;

        private ConsumerEventHandle(
                Event<T> event,
                Consumer<T> consumer
        ){
            this.event = Preconditions.checkNotNull(event);
            this.consumer = Preconditions.checkNotNull(consumer);
        }

        @Override
        public void unsubscribe(){
            event.unsubscribe(consumer);
        }

    }

}
