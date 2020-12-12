package site.intunderflow.dscript.network.message;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import site.intunderflow.dscript.network.cluster.NodeInterface;
import site.intunderflow.dscript.network.cluster.RequestException;
import site.intunderflow.dscript.network.cluster.address.Address;
import site.intunderflow.dscript.network.cluster.address.AddressBook;
import site.intunderflow.dscript.network.cluster.rest.Endpoint;
import site.intunderflow.dscript.utility.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Convenience class for handling incoming and outgoing messages.
 */
public class Router implements Consumer<Message> {

    private static final int SEEN_RECENTLY_SIZE = 100;

    private final Logger logger = Logger.getLogger(Router.class.getName());

    private final AddressBook addressBook;

    private final FixedLengthConveyor<MessageForComparison> messagesSeen;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final Event<Message> onMessage = new Event<>();

    private Endpoint endpoint;

    private String identity;

    private boolean printBroadcasts = false;

    public Router(
            AddressBook addressBook,
            Endpoint endpoint,
            String identity
    ){
        this(addressBook);
        this.endpoint = Preconditions.checkNotNull(endpoint);
        endpoint.setMessageHandler(this);
        this.identity = Preconditions.checkNotNull(identity);
    }

    public Router(
            AddressBook addressBook,
            Endpoint endpoint
    ){
        this(addressBook, endpoint, "");
    }


    public Router(
            AddressBook addressBook
    ){
        this.addressBook = Preconditions.checkNotNull(addressBook);
        this.messagesSeen = new FixedLengthConveyor<>(SEEN_RECENTLY_SIZE);
        this.identity = "";
    }

    @Override
    public void accept(Message message){
        try {
            MessageForComparison comparisonMessage = new MessageForComparison(message);
            if (messagesSeen.contains(
                    comparisonMessage
            )) {
                return;
            }
            messagesSeen.add(comparisonMessage);
            onMessage.fire(message);
            Message forFurtherBroadcast = message.getForNextBroadcast();
            if (shouldBroadcast(forFurtherBroadcast)) {
                broadcast(forFurtherBroadcast);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private boolean shouldBroadcast(Message message){
        return message.getWorkForBroadcast() > 0;
    }

    private Provider<Address> getNodesToBroadcastTo(){
        // Select a few nodes from the address book.
        // We currently select 10 or less if we don't have that many.
        int uniqueAddressesToSelect = 10;
        ArrayList<Address> aliveAddresses = addressBook.getAliveAddresses();
        if (aliveAddresses.size() < uniqueAddressesToSelect){
            uniqueAddressesToSelect = aliveAddresses.size();
        }
        if (uniqueAddressesToSelect == 0){
            logger.log(Level.WARNING, "We cannot broadcast because we have no recent alive addresses");
            return new ExhaustedProvider<>();
        }
        return new AddressFromListUniqueRandomProvider(aliveAddresses);
    }

    public void broadcastBlocking(Message message){
        doBroadcast(message);
    }

    public void broadcast(Message message){
        if (printBroadcasts) {
            System.out.println("Broadcasting " + message.getContent().getType() + " - " + message.toString());
        }
        executorService.execute(() -> {
            doBroadcast(message);
        });
    }

    public void printBroadcasts(){
        printBroadcasts = true;
    }

    private void doBroadcast(Message message){
       Provider<Address> broadcastTo = getNodesToBroadcastTo();
       int goalNodesToBroadcastTo = 10;
       int nodesBroadcastTo = 0;
       while (!broadcastTo.exhausted() && nodesBroadcastTo < goalNodesToBroadcastTo){
           Address nextAddress = broadcastTo.provide();
           NodeInterface nodeInterface = new NodeInterface(nextAddress);
           if (endpoint != null) {
               nodeInterface.setSelf(String.valueOf(endpoint.getPort()));
           }
           nodeInterface.setAddressBook(addressBook);
           boolean failed = false;
           try{
               nodeInterface.sendMessage(message);
           }
           catch(RequestException e){
               failed = true;
               logger.log(Level.INFO,
                       "Couldn't broadcast message to "
                               + nextAddress.toString()
                               + " because "
                               + e.getMessage());
           }
           if (!failed){
               nodesBroadcastTo++;
           }
       }
       if (nodesBroadcastTo < goalNodesToBroadcastTo){
           //logger.log(Level.WARNING, "Only managed to broadcast to " + nodesBroadcastTo + " nodes");
       }
    }

    @CanIgnoreReturnValue
    public Event.Handle onMessage(Consumer<Message> consumer){
        return onMessage.subscribe(consumer);
    }

}
