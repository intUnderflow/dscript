package site.intunderflow.dscript.application.consensus.dpos.realtime;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.actors.ListeningNode;
import site.intunderflow.dscript.application.blocklattice.BlockValidityChecker;
import site.intunderflow.dscript.application.blocklattice.BlockchainInterface;
import site.intunderflow.dscript.application.blocklattice.blockchain.BlockFromBlockStringFactory;
import site.intunderflow.dscript.application.blocklattice.blockchain.BlockHashFactory;
import site.intunderflow.dscript.application.blocklattice.blockchain.BlockStringFromBytesFactory;
import site.intunderflow.dscript.application.blocklattice.blockchain.Blockchain;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.Block;
import site.intunderflow.dscript.application.consensus.dpos.BlockAttestation;
import site.intunderflow.dscript.application.consensus.dpos.BlockVoteFactory;
import site.intunderflow.dscript.application.consensus.dpos.SignatureFactory;
import site.intunderflow.dscript.network.message.Message;
import site.intunderflow.dscript.network.message.MessageContent;
import site.intunderflow.dscript.network.message.content.BlockVote;
import site.intunderflow.dscript.network.message.content.Conflict;
import site.intunderflow.dscript.utility.ComparableByteArray;
import site.intunderflow.dscript.utility.FixedLengthConveyor;
import site.intunderflow.dscript.utility.hashing.SHA512;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RealtimeMonitor implements Consumer<byte[]> {

    private static final Logger logger = Logger.getLogger(RealtimeMonitor.class.getName());

    private final ListeningNode node;

    private final RealtimeConflictDetector realtimeConflictDetector;

    private final List<ComparableByteArray> conflictList;

    private final FixedLengthConveyor<ComparableByteArray> frontiers;

    private final FixedLengthConveyor<ComparableByteArray> hashesOfRecentlySeen;

    private SignatureFactory signatureFactory;

    public RealtimeMonitor(ListeningNode node){
        this.node = Preconditions.checkNotNull(node);
        this.realtimeConflictDetector = new RealtimeConflictDetector(node.getNetworkState());
        this.conflictList = new ArrayList<>();
        this.frontiers = new FixedLengthConveyor<>(100);
        this.hashesOfRecentlySeen = new FixedLengthConveyor<>(100);
        node.getLddb().onData(this);
        node.getRouter().onMessage(new MessageConsumer(this));
    }

    public void setSignatureFactory(SignatureFactory signatureFactory){
        this.signatureFactory = signatureFactory;
    }

    private boolean signatureFactoryAvailable(){
        return signatureFactory != null;
    }

    public List<byte[]> getFrontiers(){
        List<byte[]> list = new ArrayList<>();
        frontiers.getAll().forEach((object) -> {
            list.add(object.getArray());
        });
        return list;
    }

    public void pushFrontier(byte[] frontier){
        frontiers.add(new ComparableByteArray(frontier));
    }

    public void acceptFromLocal(Block block){
        // Push into our system.
        acceptBlock(block);
        // Propagate block.
        node.getLddb().broadcastNewData(block.toBytes());
    }

    public boolean recentlySeen(byte[] hash){
        ComparableByteArray comparableByteArray = new ComparableByteArray(hash);
        boolean seen = hashesOfRecentlySeen.contains(comparableByteArray);
        if (!seen){
            hashesOfRecentlySeen.add(comparableByteArray);
        }
        return seen;
    }

    private void accept(Message message){
        if (recentlySeen(SHA512.hash(
                message.toString().getBytes(StandardCharsets.UTF_8)
        ))){
            return;
        }
        MessageContent messageContent = message.getContent();
        if (messageContent.getType().equals("Conflict")){
            // Verify the conflict
            Conflict conflict = (Conflict) messageContent;
            byte[] previous = conflict.getPrevious();
            Block first = conflict.getFirstBlock();
            Block second = conflict.getSecondBlock();
            // Check each is matched to previous and valid.
            if (!Arrays.equals(first.getPreviousReference(), previous)){
                return;
            }
            if (!Arrays.equals(second.getPreviousReference(), previous)){
                return;
            }
            BlockValidityChecker validityChecker = new BlockValidityChecker(node.getNetworkState());
            if (!validityChecker.checkBlockValidNoException(first)){
                return;
            }
            if (!validityChecker.checkBlockValidNoException(second)){
                return;
            }
            ComparableByteArray firstArray = new ComparableByteArray(first.toBytes());
            ComparableByteArray secondArray = new ComparableByteArray(second.toBytes());
            conflictList.add(firstArray);
            conflictList.add(secondArray);
            // Check who we saw first.
            Block weSaw = realtimeConflictDetector.getAcceptedForReference(previous);
            VotingRound votingRound = new VotingRound(
                    previous,
                    node.getRouter()
            );
            if (signatureFactoryAvailable() && weSaw != null) {
                try {
                    BlockVote blockVote = new BlockVoteFactory(previous)
                            .voteFor(weSaw, signatureFactory);
                    Message blockVoteMessage = blockVote.toMessage(6);
                    votingRound.accept(blockVoteMessage);
                    node.getRouter().broadcast(blockVoteMessage);

                } catch (GeneralSecurityException e) {
                    logger.log(Level.WARNING, e.getMessage());
                }
            }
            finishVotingRound(votingRound);
            conflictList.remove(firstArray);
            conflictList.remove(secondArray);
        }
    }

    private void finishVotingRound(VotingRound votingRound){
        votingRound.scheduleToFinishIn(30, node.getNetworkState());
        BlockAttestation blockAttestation = votingRound.getBlockAttestationPasser().getPassed(40);
        Block winner = blockAttestation.getBlock();
        node.getNetworkState().acceptNewBlock(winner);
    }

    @Override
    public void accept(byte[] data){
        Block block = null;
        boolean success = true;
        try{
            block = new BlockFromBlockStringFactory(
                    new BlockStringFromBytesFactory(
                            data
                    ).getString()
            ).getBlock();
        }
        catch(Exception e){
            // Any non blocks will throw an exception, no big deal.
            success = false;
        }
        if (success){
            acceptBlock(block);
        }
    }

    private void acceptBlock(Block block){
        if (recentlySeen(
                new BlockHashFactory(block).hash()
        )){
            return;
        }
        if (node.getNetworkState().contains(block)){
            // If we have accepted this block there is nothing we need to do.
            return;
        }
        System.out.println("Opening " + block.getType() + " " + block);
        Blockchain blockchain = node.getNetworkState().getBlockchain(
                new BlockHashFactory(block).hash()
        );
        try{
            new BlockchainInterface(blockchain, node.getNetworkState()).checkChainValid();
        }
        catch(GeneralSecurityException e){
            System.out.println("Refusing because " + e.getMessage());
            return;
        }
        frontiers.add(new ComparableByteArray(new BlockHashFactory(block).hash()));
        boolean accepted = realtimeConflictDetector.tryAcceptAndCheckConflict(block);
        ComparableByteArray blockBytes = new ComparableByteArray(
                new BlockHashFactory(block).hash()
        );
        if (accepted) {
            // Wait 10 seconds to confirm.
            try{
                Thread.sleep(10 * 1000);
            }
            catch(InterruptedException e){}
            if (!conflictList.contains(blockBytes)) {
                // Defer to voting otherwise accept.
                node.getNetworkState().acceptNewBlock(block);
            }
        } else {
            conflictList.add(blockBytes);
            // We have a conflict.
            Block first = realtimeConflictDetector.getAcceptedForReference(block.getPreviousReference());
            System.out.println("CONFLICT");
            System.out.println("A " + first.toString());
            System.out.println("B " + block.toString());
            Conflict conflict = new Conflict(
                    block.getPreviousReference(), first, block
            );
            Message message = conflict.toMessage(6);
            node.getRouter().broadcast(message);
            VotingRound votingRound = new VotingRound(
                    block.getPreviousReference(),
                    node.getRouter()
            );
            // Send our vote too.
            if (signatureFactoryAvailable()) {
                try {
                    BlockVote blockVote = new BlockVoteFactory(block.getPreviousReference())
                            .voteFor(first, signatureFactory);
                    Message blockVoteMessage = blockVote.toMessage(6);
                    votingRound.accept(blockVoteMessage);
                    node.getRouter().broadcast(blockVoteMessage);
                } catch (GeneralSecurityException e) {
                    logger.log(Level.WARNING, e.getMessage());
                }
            }
            // Wait for voting round to end after 30 seconds.
            finishVotingRound(votingRound);
            conflictList.remove(blockBytes);
        }
    }

    private static class MessageConsumer implements Consumer<Message> {

        private final RealtimeMonitor realtimeMonitor;

        private MessageConsumer(RealtimeMonitor realtimeMonitor){
            this.realtimeMonitor = Preconditions.checkNotNull(realtimeMonitor);
        }

        @Override
        public void accept(Message message){
            realtimeMonitor.accept(message);
        }

    }

}
