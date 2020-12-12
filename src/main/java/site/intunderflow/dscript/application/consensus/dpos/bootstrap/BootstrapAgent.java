package site.intunderflow.dscript.application.consensus.dpos.bootstrap;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.actors.ListeningNode;
import site.intunderflow.dscript.application.blocklattice.BlockchainTraceback;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.Block;
import site.intunderflow.dscript.network.cluster.NodeInterface;
import site.intunderflow.dscript.network.cluster.RequestException;
import site.intunderflow.dscript.network.cluster.address.Address;
import site.intunderflow.dscript.utility.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BootstrapAgent {

    private static final int NODES_TO_GET_FRONTIERS_FROM = 3;

    private static final Logger logger = Logger.getLogger(BootstrapAgent.class.getName());

    private final ListeningNode node;

    private final IndexConfirmationAgent indexConfirmationAgent;

    public BootstrapAgent(ListeningNode node){
        this.node = Preconditions.checkNotNull(node);
        this.indexConfirmationAgent = new IndexConfirmationAgent(node);
    }

    private Provider<Address> getProvider(){
        List<Address> aliveAddresses = node.getAddressBook().getAliveAddresses();
        if (aliveAddresses.size() == 0){
            return new ExhaustedProvider<>();
        }
        else{
            return new AddressFromListUniqueRandomProvider(aliveAddresses);
        }
    }

    private List<byte[]> getFrontiers(){
        Provider<Address> addressProvider = getProvider();
        List<ComparableByteArray> frontiersFound = new ArrayList<>();
        int successAmount = 0;
        while (!addressProvider.exhausted() && successAmount < NODES_TO_GET_FRONTIERS_FROM){
            Address address = addressProvider.provide();
            NodeInterface nodeInterface = new NodeInterface(address);
            nodeInterface.setSelf(String.valueOf(node.getEndpoint().getPort()));
            nodeInterface.setAddressBook(node.getAddressBook());
            boolean success = true;
            try{
                List<byte[]> frontiers = nodeInterface.getFrontiers();
                for(byte[] frontier : frontiers){
                    ComparableByteArray comparableByteArray = new ComparableByteArray(frontier);
                    if (!frontiersFound.contains(comparableByteArray)){
                        frontiersFound.add(comparableByteArray);
                    }
                }
            }
            catch(RequestException e){
                success = false;
                e.printStackTrace();
                logger.log(Level.WARNING, e.getMessage());
            }
            if(success){
                successAmount++;
            }
        }
        List<byte[]> listToReturn = new ArrayList<>();
        frontiersFound.forEach((frontier) ->
            listToReturn.add(frontier.getArray())
        );
        return listToReturn;
    }

    private List<byte[]> excludeNonHeadFrontiers(List<byte[]> frontiers){
        List<byte[]> headFrontiers = new ArrayList<>();
        for (byte[] frontier : frontiers){
            if (node.getNetworkState().getNextAfter(frontier) == null){
                headFrontiers.add(frontier);
            }
        }
        return headFrontiers;
    }


    public void bootstrap(){
        List<byte[]> frontiers = getFrontiers();
        // For every frontier (which is a new block) we need to find the head's only.
        frontiers = excludeNonHeadFrontiers(frontiers);
        // For every head frontier, we need to query the realtime network to verify the block.
        for(byte[] frontier : frontiers){
            indexConfirmationAgent.attemptConfirm(frontier);
        }
    }

}
