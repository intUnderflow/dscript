package site.intunderflow.dscript.network.cluster.address;

import site.intunderflow.dscript.utility.Flag;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddressBookJanitorBackgroundTask {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final AddressBookJanitor addressBookJanitor;

    public AddressBookJanitorBackgroundTask(AddressBook addressBook){
        this.addressBookJanitor = new AddressBookJanitor(addressBook);
    }

    public void runTask(){
        executorService.execute(() -> {
            Flag shutdown = new Flag();
            Runtime.getRuntime().addShutdownHook(new Thread(shutdown::raise));
            while (!shutdown.isRaised()){
                addressBookJanitor.checkAll();
                // Every 4 hours 30 minutes.
                try{
                    Thread.sleep(((4 * 60 * 60) + (30 * 60)) * 1000);
                }
                catch(InterruptedException e){}
            }
        });
    }

}
