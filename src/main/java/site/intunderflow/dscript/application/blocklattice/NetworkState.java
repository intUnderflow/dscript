package site.intunderflow.dscript.application.blocklattice;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import site.intunderflow.dscript.actors.ListeningNode;
import site.intunderflow.dscript.application.blocklattice.blockchain.BlockFromBlockStringFactory;
import site.intunderflow.dscript.application.blocklattice.blockchain.BlockHashFactory;
import site.intunderflow.dscript.application.blocklattice.blockchain.BlockStringFromBytesFactory;
import site.intunderflow.dscript.application.blocklattice.blockchain.Blockchain;
import site.intunderflow.dscript.application.blocklattice.blockchain.address.BaseAddress;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.*;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.basic_tink.Receive;
import site.intunderflow.dscript.application.blocklattice.nameservice.NameService;
import site.intunderflow.dscript.application.consensus.dpos.bootstrap.IndexConfirmationAgent;
import site.intunderflow.dscript.application.consensus.dpos.realtime.RealtimeMonitor;
import site.intunderflow.dscript.application.lddb.database.Database;
import site.intunderflow.dscript.application.lddb.resource.Identifier;
import site.intunderflow.dscript.utility.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains the state of the network made of a set of blockchains with different indexes.
 * Can use LDDB to remotely fetch network state even if not known locally.
 * This class is used by other parts of the program as the source of truth as to the goings on of the network.
 */
public class NetworkState {

    private static final Logger logger = Logger.getLogger(NetworkState.class.getName());

    private final Map<ComparableByteArray, Block> state;

    private final Map<BaseAddress, byte[]> head;

    // byte[] is previous reference, Block is accepted block.
    private final Map<ComparableByteArray, Block> previousBlocks;

    private final Map<ComparableByteArray, List<ThreadSafeDataPassing<Block>>> passers;

    /** A binding of transfer blocks to receive blocks.
     *  Each transaction is made of the transfer on the senders chain and the recieve on the recievers chain.
     *  Index is transfer, value is hash of receive block.
     *  This binding is made because we look up transfers based on receive blocks a lot when validating chains.
     */
    private final Map<ComparableByteArray, byte[]> transfersToRecieves;

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private final Flag isSavingOnShutdown = new Flag();

    private IndexConfirmationAgent indexConfirmationAgent;

    private RealtimeMonitor realtimeMonitor;

    private NameService nameService;

    private Database lddb;

    private ListeningNode node;

    private boolean printAccepts = false;

    public NetworkState(
            Map<ComparableByteArray, Block> state,
            Map<BaseAddress, byte[]> head
    ){
        this.state = Preconditions.checkNotNull(state);
        this.head = Preconditions.checkNotNull(head);
        this.previousBlocks = new HashMap<>();
        this.transfersToRecieves = new HashMap<>();
        this.passers = new HashMap<>();
        setupPreviousBlocks();
        setupRecieveEntries();
        try{
            addDefaultBlocks();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void setNode(ListeningNode node){
        this.node = Preconditions.checkNotNull(node);
    }

    public ListeningNode getNode(){
        return node;
    }

    public void setRealtimeMonitor(RealtimeMonitor realtimeMonitor){
        this.realtimeMonitor = Preconditions.checkNotNull(realtimeMonitor);
    }

    public void tryTo(ExceptionThrowingRunnable thing){
        try{
            thing.run();
        }
        catch(Exception e){}
    }

    public void loadStateFromFile(File file){
        loadStateFromFile(file, true);
    }

    public void loadStateFromFile(File file, boolean setFrontiers){
        ConfReader.readConfigurationFile(
                file,
                (index, value) ->
                    tryTo(() -> {
                        acceptNewBlock(new BlockFromBlockStringFactory(
                                new BlockStringFromBytesFactory(
                                        Hex.decode(
                                                value
                                        )
                                ).getString()
                        ).getBlock(), true);
                        if (setFrontiers && realtimeMonitor != null){
                            realtimeMonitor.pushFrontier(
                                    Hex.decode(
                                            index
                                    )
                            );
                        }
                    }
                ));
        setupPreviousBlocks();
        setupRecieveEntries();
    }

    public void loadHeadFromFile(File file){
        ConfReader.readConfigurationFile(
                file,
                (index, value) -> head.put(
                        new BaseAddress(index),
                        Hex.decode(value)
                )
        );
    }



    public void saveStateToFile(File file){
        List<Dual<String, String>> lines = new ArrayList<>();
        for (Map.Entry<ComparableByteArray, Block> entry : state.entrySet()){
            lines.add(
                    new DualString(
                            Hex.encode(
                                    entry.getKey().getArray()
                            ),
                            Hex.encode(
                                    entry.getValue().toBytes()
                            )
                    )
            );
        }
        ConfWriter.writeConfigurationFile(
                file,
                new ListProvider<>(lines)
        );
    }

    public void saveHeadToFile(File file){
        List<Dual<String, String>> lines = new ArrayList<>();
        for (Map.Entry<BaseAddress, byte[]> entry : head.entrySet()){
            lines.add(
                    new DualString(
                            entry.getKey().getAddress(),
                            Hex.encode(entry.getValue())
                    )
            );
        }
        ConfWriter.writeConfigurationFile(
                file,
                new ListProvider<>(lines)
        );
    }

    public void saveOnShutdown(File stateFile, File headFile){
        if (!isSavingOnShutdown.isRaised()) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                saveStateToFile(stateFile);
                saveHeadToFile(headFile);
            }));
            isSavingOnShutdown.raise();
        }
    }

    public void addPeriodicSaving(File stateFile, File headFile){
        executorService.scheduleWithFixedDelay(
                () -> {
                    saveStateToFile(stateFile);
                    saveHeadToFile(headFile);
                },
                10,
                10,
                TimeUnit.SECONDS
        );
    }

    public void attachNameService(NameService nameService){
        this.nameService = Preconditions.checkNotNull(nameService);
    }

    private void addDefaultBlocks() throws Exception{
        Block block = new BlockFromBlockStringFactory(
            new BlockStringFromBytesFactory(
                    Resources.getGenesisBlock()
            ).getString()
        ).getBlock();
        acceptNewBlock(
            block
        );
    }

    @VisibleForTesting
    private List<String> reportHexAddressesOfAllHeldBlocks(){
        List<String> addresses = new ArrayList<>();
        for (Map.Entry<ComparableByteArray, Block> entry : state.entrySet()){
            addresses.add(Hex.encode(
                    entry.getKey().getArray()
            ));
        }
        return addresses;
    }

    private void setupPreviousBlocks(){
        for (Map.Entry<ComparableByteArray, Block> entry : state.entrySet()){
            addPrevious(entry.getValue(), true);
        }
    }

    private void setupRecieveEntries(){
        for (Map.Entry<ComparableByteArray, Block> entry : state.entrySet()){
            if (entry.getValue().getType().equals("receive")){
                transfersToRecieves.put(
                        new ComparableByteArray(((ReceiveBlock) entry.getValue()).getTransferBlock()),
                        entry.getKey().getArray()
                );
            }
        }
    }

    public boolean contains(Block block){
        return state.containsValue(block);
    }

    private void addPrevious(Block block){
        addPrevious(block, true);
    }

    private void addPrevious(Block block, boolean shouldTrace){
        if (block.isGenesis()){
            return;
        }
        byte[] previous = block.getPreviousReference();
        ThreadSafeDataPassing<Boolean> blocker = new ThreadSafeDataPassing<>();
        if (previous != null && previous.length > 0) {
            previousBlocks.put(
                    new ComparableByteArray(previous), block);
            if (shouldTrace) {
                // Trace down to the genesis.
                new BlockchainTraceback(this, (newBlock) -> {
                    if (!contains(newBlock)) {
                        acceptNewBlock(newBlock, false);
                    }
                    byte[] hash = new BlockHashFactory(block).hash();
                    if (newBlock.isGenesis() && getNextAfter(hash) == null){
                        head.put(
                                BaseAddress.forCreateBlock(
                                        (CreateBlock) newBlock
                                ),
                                hash
                        );
                    }
                    if (newBlock.isGenesis()){
                        blocker.pass(true);
                    }
                }).trace(block).blockUntilComplete();
            }
        }
        // Block until head updated.
        blocker.getPassed(60 );
    }

    public void setLddb(Database lddb){
        this.lddb = lddb;
    }

    public Block getBestGuess(byte[] index){
        if (index == null){
            return null;
        }
        Block foundLocally = getLocally(index);
        if (foundLocally == null){
            return getRemotelyBestGuess(index);
        }
        else{
            return foundLocally;
        }
    }

    public Block get(byte[] index){
        if (index == null){
            return null;
        }
        Block foundLocally = getLocally(index);
        if (foundLocally == null){
            return getRemotely(index);
        }
        else{
            return foundLocally;
        }
    }

    public Block getNextAfter(byte[] reference){
        return previousBlocks.get(new ComparableByteArray(
                reference
        ));
    }

    public Block getLocally(byte[] index){
        return state.get(
                new ComparableByteArray(index)
        );
    }

    private Block getRemotely(byte[] index){
        if (indexConfirmationAgent == null){
            return null;
        }
        else{
            indexConfirmationAgent.attemptConfirmBlocking(index);
            return getLocally(index);
        }
    }

    private Block getRemotelyBestGuess(byte[] index){
        if (lddb == null){
            return null;
        }
        else{
            byte[] content = lddb.getRemotely(
                    Identifier.forHash(index)
            );
            if (content.length == 0){
                return null; // Not found.
            }
            try{
                Block block = new BlockFromBlockStringFactory(
                        new BlockStringFromBytesFactory(
                                content
                        ).getString()
                ).getBlock();
                state.put(new ComparableByteArray(index), block);
                addPrevious(block);
                return block;
            }
            catch (Exception e){
                logger.log(Level.WARNING, e.getMessage());
            }
        }
        return null;
    }

    public byte[] getHead(BaseAddress baseAddress){
        return head.getOrDefault(baseAddress, Hex.decode(baseAddress.getAddress()));
    }

    public boolean hasBlockForPrevious(byte[] previous){
        return previousBlocks.containsKey(previous);
    }

    public void printAccepts(){
        printAccepts = true;
    }

    private void acceptNewBlock(Block block, boolean trace){
        // Redirect name blocks to the name service.
        if (printAccepts) {
            System.out.println("Accepting address " + Hex.encode(new BlockHashFactory(block).hash()) + " " + block.getType() + " " + block.toString());
        }
        if (block.getType().equals("name_registration")){
            nameService.accept((NameRegistration) block);
        }
        else {
            byte[] hash = new BlockHashFactory(block).hash();
            // We do not block repeated acceptance because we may need to assign heads and etc.
            state.put(new ComparableByteArray(hash), block);
            addPrevious(block, trace);
            passToPassers(hash, block);
            sendRecieveIfNeeded(block, hash);
        }
    }

    private void sendRecieveIfNeeded(Block block, byte[] hash){
        if (block.getType().equals("receive")){
            ReceiveBlock receiveBlock = (ReceiveBlock) block;
            transfersToRecieves.put(
                    new ComparableByteArray(
                            receiveBlock.getTransferBlock()
                    ),
                    hash
            );
        }
        else if (block.getType().equals("transfer")){
            switch(block.getAccountType()){
                case "basic_tink":
                    if (!transfersToRecieves.containsKey(
                            new ComparableByteArray(
                                    hash
                            )
                    )) {
                        try {
                            Receive receive = Receive.fromTransfer(
                                    this,
                                    (TransferBlock) block
                            );
                            lddb.broadcastNewData(receive.toBytes());
                            this.acceptNewBlock(receive);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
            }
        }
    }

    public void acceptNewBlock(Block block){
        acceptNewBlock(block, true);
    }

    public Blockchain getBlockchain(BaseAddress baseAddress){
        return new BlockchainContainer(getHead(baseAddress));
    }

    public Blockchain getBlockchain(byte[] atIndex){
        return new BlockchainContainer(atIndex);
    }

    public Block getGenesis(Block block){
        ThreadSafeDataPassing<Block> genesisPasser = new ThreadSafeDataPassing<>();
        new BlockchainTraceback(this, (found) -> {
            if (found.isGenesis()){
                genesisPasser.pass(block);
            }
        }).traceConfirmedOnly(block);
        return genesisPasser.getPassed(10);
    }

    public Block getGenesis(BaseAddress baseAddress){
        return get(baseAddress.toBytes());
    }

    public Block get(BaseAddress baseAddress){
        return get(getHead(baseAddress));
    }

    public static NetworkState newInstance(){
        return new NetworkState(
                new HashMap<>(),
                new HashMap<>()
        );
    }

    private void passToPassers(byte[] index, Block block){
        ComparableByteArray comparableByteArray = new ComparableByteArray(index);
        if (passers.containsKey(comparableByteArray)){
            List<ThreadSafeDataPassing<Block>> passersToProcess = passers.get(comparableByteArray);
            for (ThreadSafeDataPassing<Block> passer : passersToProcess){
                passer.pass(block);
            }
            passers.remove(comparableByteArray);
        }
    }

    public void enrolPassser(byte[] index, ThreadSafeDataPassing<Block> passer){
        ComparableByteArray comparableByteArray = new ComparableByteArray(index);
        if (passers.containsKey(comparableByteArray)){
            passers.get(comparableByteArray).add(passer);
        }
        else{
            List<ThreadSafeDataPassing<Block>> list = new ArrayList<>();
            list.add(passer);
            passers.put(comparableByteArray, list);
        }
    }

    public void setIndexConfirmationAgent(IndexConfirmationAgent indexConfirmationAgent){
        this.indexConfirmationAgent = indexConfirmationAgent;
    }

    private class BlockchainContainer implements Blockchain{

        private final byte[] head;

        private BlockchainContainer(
                byte[] head
        ){
            this.head = Preconditions.checkNotNull(head);
        }

        @Override
        public byte[] getHead() {
            return head;
        }
    }

}
