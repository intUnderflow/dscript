package site.intunderflow.dscript.application.lddb.database;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import site.intunderflow.dscript.actors.ListeningNode;
import site.intunderflow.dscript.application.lddb.location.LocalLocationProvider;
import site.intunderflow.dscript.application.lddb.location.LocationProvider;
import site.intunderflow.dscript.application.lddb.messages.Broadcast;
import site.intunderflow.dscript.application.lddb.messages.FindDirect;
import site.intunderflow.dscript.application.lddb.messages.LDDBMessage;
import site.intunderflow.dscript.application.lddb.messages.NetworkMessageContentFactory;
import site.intunderflow.dscript.application.lddb.resource.Identifier;
import site.intunderflow.dscript.application.lddb.resource.Location;
import site.intunderflow.dscript.network.message.Message;
import site.intunderflow.dscript.network.message.MessageContent;
import site.intunderflow.dscript.network.message.MessageWithReachFactory;
import site.intunderflow.dscript.network.message.Router;
import site.intunderflow.dscript.network.message.content.LDDBBroadcast;
import site.intunderflow.dscript.network.message.content.LDDBFindDirect;
import site.intunderflow.dscript.utility.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Based on the Lightweight Distributed Database Design Docs (LDDB)
 * The front of the database handling logic around messages.
 */
public class Database implements Consumer<Message> {

    private static final long TIMEOUT_FIND_DIRECT_SECONDS = 20;

    private final Router router;

    private final LocalStorage storage;

    private final Map<Identifier, Event<byte[]>> identifierListeners;

    private final Event<byte[]> onNewData;

    private final ExecutorService janitors = Executors.newCachedThreadPool();

    private Filter<Broadcast> broadcastFilter;

    private Filter<FindDirect> findDirectFilter;

    private LocationProvider locationProvider;

    private LocalLocationProvider localLocationProvider;

    public Database(Router router, LocalStorage storage){
        this.router = Preconditions.checkNotNull(router);
        router.onMessage(this);
        this.storage = Preconditions.checkNotNull(storage);
        this.broadcastFilter = (b) -> true;
        this.findDirectFilter = (f) -> true;
        this.identifierListeners = new HashMap<>();
        this.onNewData = new Event<>();
    }

    public LocalLocationProvider getLocalLocationProvider(){
        return (identifier, content) -> {
            if (localLocationProvider == null){
                return locationProvider.getLocation(identifier);
            }
            else{
                return localLocationProvider.getLocation(identifier, content);
            }
        };
    }

    public void broadcast(LDDBMessage lddbMessage){
        NetworkMessageContentFactory messageContentFactory = new NetworkMessageContentFactory(lddbMessage);
        Message message = new MessageWithReachFactory(messageContentFactory.toMessage()).create(6);
        router.broadcast(message);
    }

    public void broadcastNewData(byte[] data){
        broadcastNewData(data, "");
    }

    public void broadcastNewData(byte[] data, String summary){
        if (locationProvider == null){
            return;
        }
        Identifier identifier = Identifier.forData(data);
        putToStorage(identifier, data);
        Location location = getLocalLocationProvider().getLocation(identifier, data);
        Broadcast broadcast = new Broadcast(
                identifier,
                location,
                summary
        );
        broadcast(broadcast);
    }

    @Override
    public void accept(Message message){
        MessageContent content = message.getContent();
        if (content.getType().equals("LDDBBroadcast")){
            LDDBBroadcast lddbBroadcast = (LDDBBroadcast) content;
            Broadcast broadcast = lddbBroadcast.getBroadcast();
            decideWhetherToStore(broadcast);
        }
        if (content.getType().equals("LDDBFindDirect")){
            LDDBFindDirect lddbFindDirect = (LDDBFindDirect) content;
            FindDirect findDirect = lddbFindDirect.getFindDirect();
            decideWhetherToFulfill(findDirect);
        }
    }

    public void setBroadcastFilter(Filter<Broadcast> broadcastFilter) {
        this.broadcastFilter = Preconditions.checkNotNull(broadcastFilter);
    }

    public void setFindDirectFilter(Filter<FindDirect> findDirectFilter){
        this.findDirectFilter = Preconditions.checkNotNull(findDirectFilter);
    }

    public byte[] getLocal(Identifier identifier){
        return storage.get(identifier);
    }

    public byte[] getRemotely(Identifier identifier){
        System.out.println("LDDB: Remotely fetching " + identifier.toString());
        if (locationProvider == null){
            return new byte[0];
        }
        // Prepare the direct find broadcast.
        Message message = new MessageWithReachFactory(
                new FindDirect(
                        identifier,
                        locationProvider.getLocation(identifier)
                ).getMessageContent()
        ).create(6);
        // Set up the hook to see if we get a reply.
        Flag dataFound = new Flag();
        ThreadSafeDataPassing<byte[]> dataPasser = new ThreadSafeDataPassing<>();
        Event.Handle listenHandle = listenForIdentifier(
                identifier, (dataIncoming) -> {
                    if (!dataFound.isRaised()){
                        dataPasser.pass(dataIncoming);
                        dataFound.raise();
                    }
                }
        );
        // Broadcast to the network.
        router.broadcast(message);
        byte[] dataGot = dataPasser.getPassed(TIMEOUT_FIND_DIRECT_SECONDS);
        if (dataGot == null){
            return new byte[0];
        }
        listenHandle.unsubscribe();
        putToStorage(identifier, dataGot);
        return dataGot;
    }

    private void putToStorage(Identifier identifier, byte[] data){
        storage.put(identifier, data);
        onNewData.fire(data);
    }

    public byte[] get(Identifier identifier){
        byte[] local = getLocal(identifier);
        if (local.length == 0){
            return getRemotely(identifier);
        }
        else{
            return local;
        }
    }

    private void decideWhetherToFulfill(FindDirect findDirect){
        if (findDirectFilter.shouldFilter(findDirect)){
            attemptFulfill(findDirect);
        }
    }

    private void attemptFulfill(FindDirect findDirect){
        byte[] content = getLocal(
                findDirect.getIdentifier()
        );
        if (content.length == 0){
            // We don't have it
            return;
        }
        Location sendContentTo = findDirect.getReportTo();
        if (sendContentTo.canWeFulfill()){
            sendContentTo.attemptToFulfill(content);
        }
    }

    private void decideWhetherToStore(Broadcast broadcast){
        if (broadcastFilter.shouldFilter(broadcast)){
            attemptStore(broadcast);
        }
    }

    private void attemptStore(Broadcast broadcast){
        byte[] content = broadcast.getLocation().fetch();
        // Check matches size and hash of identifier.
        Identifier identifier = broadcast.getIdentifier();
        Preconditions.checkArgument(identifier.isForData(content), "Identifier mismatch.");
        putToStorage(identifier, content);
    }

    public void setLocationProvider(LocationProvider locationProvider){
        this.locationProvider = locationProvider;
    }

    public void setLocalLocationProvider(LocalLocationProvider localLocationProvider){
        this.localLocationProvider = localLocationProvider;
    }

    @CanIgnoreReturnValue
    private Event.Handle listenForIdentifier(Identifier identifier, Consumer<byte[]> validDataConsumer){
        if (!identifierListeners.containsKey(identifier)){
            identifierListeners.put(identifier, new Event<>());
        }
        return identifierListeners.get(identifier).subscribe(validDataConsumer);
    }


    public boolean checkIfIdentifierPendingDirectFind(Identifier identifier){
        return identifierListeners.containsKey(identifier);
    }

    public void fulfillDirectFinds(Identifier identifier, byte[] data){
        if (identifierListeners.containsKey(identifier)) {
            Event<byte[]> event = identifierListeners.get(identifier);
            event.fire(data);
            janitors.execute(() -> {
                try{
                    Thread.sleep(2000);
                }
                catch(InterruptedException e){}
                if (event.getSubscriberCount() == 0){
                    identifierListeners.remove(identifier);
                }
            });
        }
    }

    @CanIgnoreReturnValue
    public Event.Handle onData(Consumer<byte[]> consumer){
        return onNewData.subscribe(consumer);
    }

    public LocalStorage getStorage(){
        return storage;
    }

}
