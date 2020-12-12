package site.intunderflow.dscript.application.consensus.dpos.realtime;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.application.blocklattice.NetworkState;
import site.intunderflow.dscript.application.blocklattice.blockchain.address.BaseAddress;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.Block;
import site.intunderflow.dscript.application.consensus.dpos.BlockAttestation;
import site.intunderflow.dscript.network.message.Message;
import site.intunderflow.dscript.network.message.MessageContent;
import site.intunderflow.dscript.network.message.Router;
import site.intunderflow.dscript.network.message.content.BlockVote;
import site.intunderflow.dscript.utility.ThreadSafeDataPassing;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class VotingRound implements Consumer<Message> {

    private final byte[] issue;

    private final Map<Block, BlockAttestation> signatures;

    private final List<BaseAddress> signed;

    private final ThreadSafeDataPassing<BlockAttestation> blockAttestationPasser;

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public VotingRound(byte[] issue, Router router){
        this.issue = Preconditions.checkNotNull(issue);
        signatures = new HashMap<>();
        signed = new ArrayList<>();
        blockAttestationPasser = new ThreadSafeDataPassing<>();
        router.onMessage(this);
    }

    /**
     * Returns the winning blocks and its signatures.
     */
    public BlockAttestation finishRound(NetworkState networkState){
        boolean isFirst = true;
        BlockAttestation winner = null;
        long winnerWeight = -1;
        for (Map.Entry<Block, BlockAttestation> attestation : signatures.entrySet()){
            long thisWeight = attestation.getValue().getAttestationConfirmedWeight(networkState);
            if (isFirst || winnerWeight < thisWeight){
                isFirst = false;
                winnerWeight = thisWeight;
                winner = attestation.getValue();
            }
        }
        blockAttestationPasser.pass(winner);
        return winner;
    }

    public void scheduleToFinishIn(int seconds, NetworkState networkState){
        executorService.scheduleWithFixedDelay(
                () -> {
                    finishRound(networkState);
                    executorService.shutdown();
                },
                seconds,
                seconds,
                TimeUnit.SECONDS
        );
    }

    public ThreadSafeDataPassing<BlockAttestation> getBlockAttestationPasser() {
        return blockAttestationPasser;
    }

    @Override
    public void accept(Message message){
        MessageContent messageContent = message.getContent();
        if (messageContent.getType().equals("BlockVote")){
            BlockVote blockVote = (BlockVote) messageContent;
            accept(blockVote);
        }
    }

    private void accept(BlockVote blockVote){
        if (!Arrays.equals(blockVote.getOnIssue(), issue)){
            return;
        }
        if (signed.contains(blockVote.getSignature().getAddress())){
            return;
        }
        acceptNotDuplicate(blockVote);
    }

    private void acceptNotDuplicate(BlockVote blockVote){
        signed.add(blockVote.getSignature().getAddress());
        if (!signatures.containsKey(blockVote.getAcceptedBlock())){
            signatures.put(blockVote.getAcceptedBlock(), new BlockAttestation(blockVote.getAcceptedBlock()));
        }
        signatures.get(blockVote.getAcceptedBlock()).addSignature(blockVote.getSignature());
    }
}
