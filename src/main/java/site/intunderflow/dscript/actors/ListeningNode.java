package site.intunderflow.dscript.actors;

import site.intunderflow.dscript.application.bcra.BCRARound;
import site.intunderflow.dscript.application.bcra.commitment.ExecutorCommitmentFactory;
import site.intunderflow.dscript.application.bcra.executor.ExecutorIdentity;
import site.intunderflow.dscript.application.blocklattice.NetworkState;
import site.intunderflow.dscript.application.blocklattice.commander.Commander;
import site.intunderflow.dscript.application.blocklattice.nameservice.NameService;
import site.intunderflow.dscript.application.consensus.dpos.SignatureFactory;
import site.intunderflow.dscript.application.consensus.dpos.bootstrap.BootstrapBackgroundTask;
import site.intunderflow.dscript.application.consensus.dpos.bootstrap.IndexConfirmationAgent;
import site.intunderflow.dscript.application.consensus.dpos.realtime.RealtimeMonitor;
import site.intunderflow.dscript.application.dapp_website.DAppWebsiteServer;
import site.intunderflow.dscript.application.executor.BackgroundService;
import site.intunderflow.dscript.application.lddb.database.Database;
import site.intunderflow.dscript.application.lddb.database.LocalStorage;
import site.intunderflow.dscript.application.lddb.location.LocationProvider;
import site.intunderflow.dscript.network.cluster.Bootstrap;
import site.intunderflow.dscript.network.cluster.address.Address;
import site.intunderflow.dscript.network.cluster.address.AddressBook;
import site.intunderflow.dscript.network.cluster.address.AddressBookJanitorBackgroundTask;
import site.intunderflow.dscript.network.cluster.rest.Endpoint;
import site.intunderflow.dscript.network.message.Message;
import site.intunderflow.dscript.network.message.MessageContent;
import site.intunderflow.dscript.network.message.Router;
import site.intunderflow.dscript.network.message.content.AddressAnnouncement;
import site.intunderflow.dscript.utility.FileStorageLocation;

import java.io.File;
import java.util.List;

/**
 * Listens to the network for address announcement and bootstraps itself to the network.
 */
public class ListeningNode {

    private static final String LDDB_PATH = FileStorageLocation.getFileStorageLocationWithFolder("lddb");

    private final int port;

    private final Bootstrap bootstrap;

    private final DAppWebsiteServer dAppWebsiteServer;

    private final Endpoint endpoint;

    private final AddressBook addressBook;

    private final Router router;

    private final Database lddb;

    private final NetworkState networkState;

    private final RealtimeMonitor realtimeMonitor;

    private final NameService nameService;

    private BCRARound bcraRound;

    public ListeningNode(
            int port,
            List<Address> bootstrapNodes
    ){
        this.port = port;
        this.addressBook = new AddressBook();
        this.endpoint = new Endpoint(this);
        this.router = new Router(addressBook, endpoint, String.valueOf(port));
        this.lddb = new Database(router,
                new LocalStorage(
                        LDDB_PATH + port + ".conf"
                ));
        this.bootstrap = new Bootstrap(
                endpoint,
                addressBook,
                bootstrapNodes,
                lddb
        );
        this.networkState = NetworkState.newInstance();
        networkState.setLddb(lddb);
        networkState.setNode(this);
        this.nameService = new NameService();
        nameService.setFileStorageLocation(FileStorageLocation.getFileStorageLocation() + "names.conf");
        nameService.loadFromFileIfPossible();
        networkState.attachNameService(nameService);
        this.realtimeMonitor = new RealtimeMonitor(this);
        networkState.setRealtimeMonitor(realtimeMonitor);
        networkState.setIndexConfirmationAgent(new IndexConfirmationAgent(this));
        this.dAppWebsiteServer = new DAppWebsiteServer(networkState, nameService);
        endpoint.setdAppWebsiteServer(dAppWebsiteServer);
        enable();
    }

    public ListeningNode(
            int port
    ){
        this(port, null);
    }

    public void makeMeAnExecutor(ExecutorCommitmentFactory factory){
        this.bcraRound = new BCRARound(this);
        bcraRound.schedule();
        bcraRound.commitIdentityForEachRound(factory);
        bcraRound.setIdentity(factory.getSignatureFactory());
        new BackgroundService(
                router,
                bcraRound,
                networkState,
                lddb,
                factory.getSignatureFactory()
        );
        File bcraFile = new File(
                FileStorageLocation.getFileStorageLocationWithFolder("bcra") + port + "bcra.conf"
        );
        if (bcraFile.exists()){
            bcraRound.loadFromFile(bcraFile);
        }
        bcraRound.addPeriodicSaving(bcraFile);
        bcraRound.saveOnShutdown(bcraFile);
    }

    private void enable(){
        endpoint.setup(port);
        endpoint.setLDDBDatabase(lddb);
        router.onMessage(this::onMessage);
        bootstrap.bootstrap();
        new BootstrapBackgroundTask(this).runTask();
        new AddressBookJanitorBackgroundTask(this.getAddressBook()).runTask();
        File stateFile = new File(
                FileStorageLocation.getFileStorageLocationWithFolder("state") + port + "state.conf"
        );
        if (stateFile.exists()){
            networkState.loadStateFromFile(stateFile);
        }
        File headFile = new File(
                FileStorageLocation.getFileStorageLocationWithFolder("state") + port + "head.conf"
        );
        if (headFile.exists()){
            networkState.loadHeadFromFile(headFile);
        }
        networkState.saveOnShutdown(stateFile, headFile);
        networkState.addPeriodicSaving(stateFile, headFile);
    }

    private void onMessage(Message message){
        MessageContent messageContent = message.getContent();
        if (messageContent.getType().equals("AddressAnnouncement")){
            AddressAnnouncement addressAnnouncement = (AddressAnnouncement) messageContent;
            Address address = addressAnnouncement.getAddress();
            addressBook.addAddress(address);
        }
    }

    public void setRealtimeMonitorSignatureFactory(SignatureFactory signatureFactory){
        realtimeMonitor.setSignatureFactory(signatureFactory);
    }

    public AddressBook getAddressBook(){
        return addressBook.copy();
    }

    public Database getLddb(){ return lddb; }

    public NetworkState getNetworkState(){ return networkState; }

    public int getPort(){ return port; }

    public Endpoint getEndpoint(){ return endpoint; }

    public Router getRouter(){ return router; }

    public NameService getNameService() {
        return nameService;
    }

    public RealtimeMonitor getRealtimeMonitor() {
        return realtimeMonitor;
    }

    public BCRARound getBcraRound() {
        return bcraRound;
    }

}
