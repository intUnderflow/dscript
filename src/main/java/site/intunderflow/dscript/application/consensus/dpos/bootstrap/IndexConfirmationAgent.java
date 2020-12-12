package site.intunderflow.dscript.application.consensus.dpos.bootstrap;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.actors.ListeningNode;
import site.intunderflow.dscript.application.blocklattice.BlockchainTraceback;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.Block;
import site.intunderflow.dscript.utility.ThreadSafeDataPassing;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IndexConfirmationAgent {

    private final ListeningNode node;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public IndexConfirmationAgent(ListeningNode node){
        this.node = Preconditions.checkNotNull(node);
    }

    public void attemptConfirmBlocking(byte[] index){
        attemptConfirmInternal(index, true);
    }

    public void attemptConfirm(byte[] index){
        attemptConfirmInternal(index, false);
    }

    private void attemptConfirmInternal(byte[] index, boolean blocking){
        ThreadSafeDataPassing<Boolean> blocker = new ThreadSafeDataPassing<>();
        Block block = node.getNetworkState().getBestGuess(index);
        if (block == null){
            return;
        }
        node.getRealtimeMonitor().acceptFromLocal(block);
        // Now we wait for the block to confirm into the NetworkState, once confirmed,
        // we have confirmed all it's children.
        executorService.execute(() -> {
            ThreadSafeDataPassing<Block> passer = new ThreadSafeDataPassing<>();
            node.getNetworkState().enrolPassser(index, passer);
            Block blockConfirmed = passer.getPassed(60);
            if (blockConfirmed == null){
                return;
            }
            // Now confirm all the children.
            new BlockchainTraceback(node.getNetworkState(), (child) -> node.getNetworkState().acceptNewBlock(child)).trace(blockConfirmed);
            blocker.pass(true);
        });
        if (blocking){
            blocker.getPassed(60);
        }
    }


}
