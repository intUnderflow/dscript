package site.intunderflow.dscript.network.cluster;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import site.intunderflow.dscript.application.lddb.database.Database;
import site.intunderflow.dscript.application.lddb.location.EndpointLocationProvider;
import site.intunderflow.dscript.application.lddb.location.HexProvider;
import site.intunderflow.dscript.network.cluster.address.Address;
import site.intunderflow.dscript.network.cluster.address.AddressBook;
import site.intunderflow.dscript.network.cluster.rest.Endpoint;
import site.intunderflow.dscript.network.message.Message;
import site.intunderflow.dscript.network.message.Router;
import site.intunderflow.dscript.network.message.content.AddressAnnouncement;
import site.intunderflow.dscript.utility.AddressFromListUniqueRandomProvider;
import site.intunderflow.dscript.utility.Flag;
import site.intunderflow.dscript.utility.Immutables;
import site.intunderflow.dscript.utility.Provider;

import javax.xml.crypto.Data;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 1. Connects to bootstrap node
 * 2. Discover our own address
 * 3. Asks node for addresses seen recently.
 * 4. Sends our own address as a message.*/
public class Bootstrap {

    private static final ImmutableList<Address> BOOTSTRAP_NODES = ImmutableList.of(
            Address.fromString("127.0.0.1:8081")
    );

    private static final Logger logger = Logger.getLogger(Bootstrap.class.getName());

    private final ImmutableList<Address> bootstrapNodes;

    private final Endpoint endpoint;

    private final AddressBook addressBook;

    private final Database lddb;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private Address ownAddress;

    public Bootstrap(
            Endpoint endpoint,
            AddressBook addressBook,
            Database lddb
    ){
        this(endpoint, addressBook, BOOTSTRAP_NODES, lddb);
    }

    public Bootstrap(
            Endpoint endpoint,
            AddressBook addressBook,
            List<Address> bootstrapNodes,
            Database lddb
    ){
        this.endpoint = Preconditions.checkNotNull(endpoint);
        this.addressBook = Preconditions.checkNotNull(addressBook);
        if (bootstrapNodes == null){
            this.bootstrapNodes = BOOTSTRAP_NODES;
        }
        else{
            this.bootstrapNodes = ImmutableList.copyOf(Preconditions.checkNotNull(bootstrapNodes));
        }
        this.lddb = Preconditions.checkNotNull(lddb);
    }

    public void bootstrap(Address node) throws RequestException {
        NodeInterface nodeInterface = new NodeInterface(node);
        nodeInterface.setSelf(String.valueOf(endpoint.getPort()));
        nodeInterface.setAddressBook(addressBook);
        if (ownAddress == null){
            // TODO: Support path addresses for own node.
            ownAddress = nodeInterface.getOwnAddress(
                    endpoint.getPort(),
                    ""
            );
            addressBook.exclude(ownAddress);
            lddb.setLocationProvider(
                    new EndpointLocationProvider(
                            ownAddress
                    )
            );
            lddb.setLocalLocationProvider(
                    new HexProvider()
            );
        }
        nodeInterface.discoverAddresses(addressBook);
        Message addressAnnouncement = new AddressAnnouncement(
                ownAddress
        ).toMessage(6);
        Router router = new Router(addressBook);
        router.broadcast(addressAnnouncement);
        executorService.execute(() -> {
            try{
                Thread.sleep(10 * 1000);
            }
            catch(InterruptedException e){}
            router.broadcast(addressAnnouncement);
            Flag shutdown = new Flag();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                shutdown.raise();
            }));
            if (!shutdown.isRaised()){
                try{
                    Thread.sleep(60* 1000);
                }
                catch(InterruptedException e){}
                router.broadcast(addressAnnouncement);
            }
        });
    }

    public void bootstrap(){
        Immutables<Address> immutables = new Immutables<>();
        List<Address> list = immutables.cloneToMutableList(bootstrapNodes);
        Provider<Address> provider = new AddressFromListUniqueRandomProvider(
                list
        );
        boolean success = false;
        while (!success && !provider.exhausted()){
            try{
                bootstrap(provider.provide());
                success = true;
            }
            catch(RequestException e){
                logger.log(Level.WARNING, "Bootstrap node failed! " + e.getMessage());
            }
        }
        if (provider.exhausted() && list.size() > 0){
            logger.log(Level.SEVERE, "Failed to bootstrap! All nodes failed!");
        }
    }

}
