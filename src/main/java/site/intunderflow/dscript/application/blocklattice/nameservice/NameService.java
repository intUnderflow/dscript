package site.intunderflow.dscript.application.blocklattice.nameservice;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.application.blocklattice.blockchain.address.BaseAddress;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.NameRegistration;
import site.intunderflow.dscript.utility.ConfReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

/** NameService associates string names with BaseAddress references on a first come first serve basis. */
public class NameService {

    private final Map<String, BaseAddress> currentlyAccepted;

    private String fileStorageLocation;

    public NameService(){
        this(new HashMap<>());
    }

    public NameService(Map<String, BaseAddress> currentlyAccepted){
        this.currentlyAccepted = Preconditions.checkNotNull(currentlyAccepted);
        Runtime.getRuntime().addShutdownHook(new SaveHook(this));
    }

    public BaseAddress getForName(String name){
        return currentlyAccepted.get(name);
    }

    public void accept (String name, BaseAddress address){
        System.out.println("Binding name " + name + " to " + address.getAddress());
        currentlyAccepted.put(name, address);
        new SaveHook(this).run();
    }

    public void accept(NameRegistration nameRegistration){
        accept(nameRegistration.getName(), nameRegistration.getAddress());
    }

    public void setFileStorageLocation(String location){
        this.fileStorageLocation = location;
    }

    public void loadFromFileIfPossible(){
        if (fileStorageLocation == null){
            return;
        }
        File file = new File(fileStorageLocation);
        if (file.exists()){
            readFromFile(file);
        }
    }

    private void readFromFile(File file){
        ConfReader.readConfigurationFile(
                file,
                (index, value) -> {
                    currentlyAccepted.put(
                            index,
                            new BaseAddress(value)
                    );
                }
        );
    }

    private class SaveHook extends Thread {

        private final NameService nameService;

        public SaveHook(NameService nameService){
            this.nameService  = Preconditions.checkNotNull(nameService);
        }

        @Override
        public void run(){
            if (nameService.fileStorageLocation != null){
                try{
                    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(
                            new File(
                                    nameService.fileStorageLocation
                            )
                    ));
                    boolean firstWrite = true;
                    for (Map.Entry<String, BaseAddress> entry : nameService.currentlyAccepted.entrySet()){
                        if (!firstWrite){
                            bufferedWriter.write("\r\n");
                        }
                        bufferedWriter.write(encodeLine(entry.getKey(), entry.getValue()));
                        firstWrite = false;
                    }
                    bufferedWriter.close();
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        }

    }

    private String encodeLine(String name, BaseAddress baseAddress){
        return name + "=" + baseAddress.getAddress();
    }

}
