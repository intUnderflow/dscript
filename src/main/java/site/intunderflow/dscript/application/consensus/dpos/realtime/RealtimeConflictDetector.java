package site.intunderflow.dscript.application.consensus.dpos.realtime;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.application.blocklattice.NetworkState;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.Block;
import site.intunderflow.dscript.utility.ComparableByteArray;
import site.intunderflow.dscript.utility.Time;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RealtimeConflictDetector {

    private static final int GARBAGE_TIME = 300;

    private static final int COLLECT_GARBAGE_EVERY_SECONDS = 60;

    /** byte[] is the previous block reference, block is the block we accept for this. */
    private final Map<ComparableByteArray, Block> acceptedBlockedForPrevious = new HashMap<>();

    private final Map<ComparableByteArray, Long> lastSeen = new HashMap<>();

    private final ScheduledExecutorService executor;

    private final NetworkState networkState;

    public RealtimeConflictDetector(NetworkState networkState){
        this.networkState = Preconditions.checkNotNull(networkState);
        this.executor = Executors.newSingleThreadScheduledExecutor();
        setUpBackgroundGarbageCollector();
    }

    private boolean blocksEqual(Block a, Block b){
        return Arrays.equals(
                a.toBytes(),
                b.toBytes()
        );
    }

    public boolean tryAcceptAndCheckConflict(Block newBlock){
        if (newBlock.getPreviousReference() == null || newBlock.getPreviousReference().length == 0){
            // Start account always acceptable.
            return true;
        }
        if (networkState.hasBlockForPrevious(newBlock.getPreviousReference())){
            return blocksEqual(networkState.getNextAfter(newBlock.getPreviousReference()), newBlock);
        }
        else if (acceptedBlockedForPrevious.containsKey(
                new ComparableByteArray(newBlock.getPreviousReference()
                ))){
            return blocksEqual(newBlock, acceptedBlockedForPrevious.get(
                    new ComparableByteArray(newBlock.getPreviousReference())
            ));
        }
        else{
            acceptedBlockedForPrevious.put(new ComparableByteArray(newBlock.getPreviousReference()), newBlock);
            return true;
        }
    }

    public Block getAcceptedForReferenceLocal(byte[] reference){
        return acceptedBlockedForPrevious.get(reference);
    }

    public Block getAcceptedForReference(byte[] reference){
        if (acceptedBlockedForPrevious.containsKey(new ComparableByteArray(reference))){
            return acceptedBlockedForPrevious.get(new ComparableByteArray(reference));
        }
        else{
            return networkState.getNextAfter(reference);
        }
    }

    private void setUpBackgroundGarbageCollector(){
        executor.scheduleAtFixedRate(this::collectGarbage, 1, COLLECT_GARBAGE_EVERY_SECONDS, TimeUnit.SECONDS);
    }

    private void collectGarbage(){
        long currentTime = Time.getUTCTimestamp();
        for (Map.Entry<ComparableByteArray, Long> entry : lastSeen.entrySet()){
            if (entry.getValue() + GARBAGE_TIME < currentTime){
                // We can accept these now.
                networkState.acceptNewBlock(acceptedBlockedForPrevious.get(entry.getKey()));
                acceptedBlockedForPrevious.remove(entry.getKey());
                lastSeen.remove(entry.getKey());
            }
        }
    }

}
