package site.intunderflow.dscript.application.consensus.dpos.bootstrap;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.actors.ListeningNode;
import site.intunderflow.dscript.utility.Flag;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BootstrapBackgroundTask {

    private final BootstrapAgent bootstrapAgent;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public BootstrapBackgroundTask(ListeningNode node){
        this.bootstrapAgent = new BootstrapAgent(Preconditions.checkNotNull(node));
    }

    public void runTask(){
        executorService.execute(() -> {
            Flag shutdown = new Flag();
            Runtime.getRuntime().addShutdownHook(new Thread(shutdown::raise));
            try{Thread.sleep(15 * 1000);}
            catch (InterruptedException e){}
            while (!shutdown.isRaised()){
                bootstrapAgent.bootstrap();
                try{
                    Thread.sleep(300 * 1000);
                }
                catch(InterruptedException e){}
            }
        });
    }

}
