package site.intunderflow.dscript.application.blocklattice;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.Block;
import site.intunderflow.dscript.utility.ThreadSafeDataPassing;

import java.util.function.Consumer;

/**
 * Traces from a block down its previous blocks all the way to the genesis.
 */
public class BlockchainTraceback {

    private final NetworkState networkState;

    private final Consumer<Block> consumer;

    private final ThreadSafeDataPassing<Boolean> threadSafeDataPassing = new ThreadSafeDataPassing<>();

    public BlockchainTraceback(NetworkState networkState, Consumer<Block> consumer){
        this.networkState = Preconditions.checkNotNull(networkState);
        this.consumer = Preconditions.checkNotNull(consumer);
    }

    private void acceptAndNextConfirmedOnly(Block block){
        if (block == null){
            return;
        }
        consumer.accept(block);
        if (!block.isGenesis()){
            acceptAndNextConfirmedOnly(networkState.getLocally(block.getPreviousReference()));
        }
    }

    public void traceConfirmedOnly(byte[] block){
        acceptAndNextConfirmedOnly(networkState.getLocally(block));
    }

    public void traceConfirmedOnly(Block block){
        acceptAndNextConfirmedOnly(block);
    }

    private void acceptAndNext(Block block){
        if (block == null){
            threadSafeDataPassing.pass(true);
            return;
        }
        consumer.accept(block);
        if (!block.isGenesis()){
            acceptAndNext(networkState.get(block.getPreviousReference()));
        }
        else{
            threadSafeDataPassing.pass(true);
        }
    }

    @CanIgnoreReturnValue
    public BlockchainTraceback trace(byte[] block){
        acceptAndNext(networkState.get(block));
        return this;
    }

    @CanIgnoreReturnValue
    public BlockchainTraceback trace(Block block){
        acceptAndNext(block);
        return this;
    }

    public void blockUntilComplete(){
        threadSafeDataPassing.getPassed(60 * 10000);
    }

}
