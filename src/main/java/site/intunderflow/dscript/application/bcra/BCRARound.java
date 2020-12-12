package site.intunderflow.dscript.application.bcra;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.actors.ListeningNode;
import site.intunderflow.dscript.application.GenesisAccounts;
import site.intunderflow.dscript.application.bcra.commitment.*;
import site.intunderflow.dscript.application.blocklattice.BlockchainInterface;
import site.intunderflow.dscript.application.blocklattice.blockchain.Blockchain;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.CreateBlock;
import site.intunderflow.dscript.application.consensus.dpos.SignatureFactory;
import site.intunderflow.dscript.application.lddb.resource.Identifier;
import site.intunderflow.dscript.network.message.Message;
import site.intunderflow.dscript.network.message.MessageContent;
import site.intunderflow.dscript.network.message.Router;
import site.intunderflow.dscript.network.message.content.BCRAAffirm;
import site.intunderflow.dscript.network.message.content.BCRACommitment;
import site.intunderflow.dscript.network.message.content.BCRAList;
import site.intunderflow.dscript.utility.*;
import site.intunderflow.dscript.utility.crypto.keys.PublicKey;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * BCRA runs every 10 minutes.
 * The first 2 minutes is broadcast.
 * Next 1 minute is consolidate.
 * Next 3 minutes is resolve.
 * Next 4 minutes is affirm.
 */
public class BCRARound {

    public static final long BCRA_DURATION_SECONDS = 60 * 5;

    private static final int DIFFICULTY_REQUIRED = 4;

    private static final Logger logger = Logger.getLogger(BCRARound.class.getName());

    private final ScheduledExecutorService periodicSavingExecutorService = Executors.newSingleThreadScheduledExecutor();

    private final List<ExecutorCommitmentFactory> toCommitEachRound;

    private final Event<Long> onBroadcast = new Event<>();

    private final Event<Long> onConsolidation = new Event<>();

    private final Event<Long> onResolve = new Event<>();

    private final Event<Long> onAffirm = new Event<>();

    private final Event<ExecutorCommitment> onNewCommitment = new Event<>();

    private final Event<SignedCommitmentList> onNewProposedList = new Event<>();

    private final Event<AffirmationSignature> onNewAffirm = new Event<>();

    private final Event<Long> onCurrentRoundChange = new Event<>();

    private final Flag isScheduled = new Flag();

    private final Flag roundBegun = new Flag();

    private final Flag isSavingPeriodically = new Flag();

    private final Flag isSavingOnShutdown = new Flag();

    private final ScheduledExecutorService eventExecutor = Executors.newSingleThreadScheduledExecutor();

    private final ListeningNode node;

    private final Map<Long, CommitmentList> winners;

    private SignatureFactory ourIdentity;

    private CommitmentList commitmentList;

    private ListResolver listResolver;

    private BCRARoundState lastStateSeen;

    private SignedCommitmentList ourProposedWinner;

    private Map<ComparableByteArray, ThreadSafeSummation> stakePerWinner;

    private Map<ComparableByteArray, Identifier> lddbWinnerMapping;

    private long currentRound;

    public BCRARound(ListeningNode node){
        this.node = Preconditions.checkNotNull(node);
        this.toCommitEachRound = new ArrayList<>();
        this.winners = new HashMap<>();
        this.commitmentList = new CommitmentList();
        resetOurList();
        onBroadcast.subscribeRunnable(this::commitAll);
        onBroadcast.subscribeRunnable(this::resetOurListResolver);
        onConsolidation.subscribeRunnable(this::sendOurList);
        onResolve.subscribeRunnable(this::resolveOurWinnerProposal);
        onAffirm.subscribeRunnable(this::affirmOurWinner);
        onAffirm.subscribeRunnable(this::resetOurList);
        onNewCommitment.subscribe((a) -> {
            commitmentList.add(a);
        });
        onNewProposedList.subscribe((a) -> {
            if (listResolver != null){
                listResolver.add(a);
            }
        });
        onNewAffirm.subscribe(this::attemptToAddAffirmation);
        this.node.getRouter().onMessage((message) -> {
            MessageContent messageContent = message.getContent();
            if (messageContent.getType().equals("BCRACommitment")){
                if (lastStateSeen == BCRARoundState.BROADCAST){
                    BCRACommitment bcraCommitment = (BCRACommitment) messageContent;
                    onNewCommitment.fire(bcraCommitment.getExecutorCommitment());
                }
                else{
                    logger.log(Level.INFO, "Refusing commitment because round is not BROADCAST, round is " +
                            lastStateSeen.name());
                }
            }
            else if (messageContent.getType().equals("BCRAList")){
                if (lastStateSeen == BCRARoundState.CONSOLIDATION){
                    BCRAList bcraList = (BCRAList) messageContent;
                    onNewProposedList.fire(bcraList.getSignedCommitmentList());
                }
                else{
                    logger.log(Level.INFO, "Refusing list because round is not CONSOLIDATION, round is " +
                            lastStateSeen.name());
                }
            }
            else if (messageContent.getType().equals("BCRAAffirm")){
                if (lastStateSeen == BCRARoundState.AFFIRM){
                    BCRAAffirm bcraAffirm = (BCRAAffirm) messageContent;
                    onNewAffirm.fire(bcraAffirm.getAffirmationSignature());
                }
                else{
                    logger.log(Level.INFO, "Refusing affirmation because round is not AFFIRM, round is" +
                            lastStateSeen.name());
                }
            }
        });
    }

    public void schedule(){
        Preconditions.checkState(!isScheduled.isRaised(), "Round already running on scheduled executor!");
        this.eventExecutor.scheduleAtFixedRate(
                this::checkEvents,
                0,
                10,
                TimeUnit.SECONDS
        );
        isScheduled.raise();
    }

    private void affirmOurWinner(){
        if (ourIdentity != null){
            try{
                AffirmationSignature ourSignature = new AffirmationSignature(
                        ourIdentity, ourProposedWinner.getList()
                );
                this.getRouter().broadcast(new BCRAAffirm(ourSignature).toMessage(6));
            }
            catch(GeneralSecurityException e){
                logger.log(Level.WARNING, "Unable to affirm our list because " + e.getMessage());
            }
        }
    }

    private void attemptToAddAffirmation(AffirmationSignature affirmation){
        ComparableByteArray index = new ComparableByteArray(
                affirmation.getSubject().getHash()
        );
        CreateBlock createBlock = (CreateBlock) node.getNetworkState().getGenesis(
                affirmation.getSignature().getAddress()
        );
        // Every stakeholder must have a public key to sign this.
        if (!createBlock.getInitializationParams().containsKey("publicKey")){
            logger.log(Level.INFO, "Refusing affirmation because of no publicKey");
            return;
        }
        PublicKey publicKey = (PublicKey) createBlock.getInitializationParams().get("publicKey");
        if (!publicKey.verifySignature(
                affirmation.getSignature().getSignature(),
                affirmation.getSubject().getHash()
        )){
            logger.log(Level.INFO, "Refusing affirmation because invalid signature");
            return;
        }
        Blockchain stakeholderChain = node.getNetworkState().getBlockchain(
                affirmation.getSignature().getAddress()
        );
        BlockchainInterface blockchainInterface = new BlockchainInterface(
                stakeholderChain, node.getNetworkState()
        );
        long stake = blockchainInterface.getAccountConfirmedValueAtTime(getRoundStakeCutoffTime(
                getCurrentRound()
        ));
        if (stake > 0) {
            if (!stakePerWinner.containsKey(index)){
                stakePerWinner.put(index, new ThreadSafeSummation());
                lddbWinnerMapping.put(index, affirmation.getSubject());
            }
            stakePerWinner.get(index).add(stake);
            if (stakePerWinner.get(index).peekSum() > (GenesisAccounts.getGenesisMainAmount() / 2)){
                confirmWinner(affirmation.getSubject());
            }
        }
    }

    private void confirmWinner(Identifier identifier){
        CommitmentList winner;
        if (!identifier.isForData(ourProposedWinner.getList().toString().getBytes(StandardCharsets.UTF_8))){
            winner = CommitmentList.fromString(
                    new String(node.getLddb().get(identifier), StandardCharsets.UTF_8)
            );
        }
        else{
            winner = ourProposedWinner.getList();
        }
        System.out.println("BCRA round " + getCurrentRound() + " winning list selected (n="
                + winner.getCommitments().size() + ")");
        putWinner(getCurrentRound(), winner);
    }

    public void setIdentity(SignatureFactory identity){
        System.out.println("BCRA identity set.");
        this.ourIdentity = identity;
    }

    private void resolveOurWinnerProposal(){
        if (listResolver != null) {
            ourProposedWinner = listResolver.getWinner();
        }
    }

    private void sendOurList(){
        if (ourIdentity == null){
            return;
        }
        try {
            SignedCommitmentList signedCommitmentList = new SignedCommitmentList(
                    commitmentList,
                    ourIdentity
            );
            BCRAList bcraList = new BCRAList(signedCommitmentList);
            Message toTransmit = bcraList.toMessage(6);
            getRouter().broadcast(toTransmit);
        }
        catch(GeneralSecurityException e){
            logger.log(Level.WARNING, "Failed to transmit our signed commitment list because: " + e.getMessage());
        }
    }

    private void resetOurListResolver(){
        listResolver = new ListResolver(node.getNetworkState());
        stakePerWinner = new HashMap<>();
        lddbWinnerMapping = new HashMap<>();
    }

    private void resetOurList() {
        // Reset in prep for next round.
        commitmentList = new CommitmentList();
    }

    private void checkEvents(){
        long currentRound = getCurrentRound();
        if (currentRound != this.currentRound){
            this.currentRound = currentRound;
            onCurrentRoundChange.fire(currentRound);
        }
        BCRARoundState newState = getCurrentRoundState();
        if (newState != lastStateSeen){
            lastStateSeen = newState;
            // Dont fire events until the first BCRA round we are a full part of begins.
            if (!roundBegun.isRaised()){
                if (newState == BCRARoundState.BROADCAST){
                    roundBegun.raise();
                }
                else{
                    System.out.println("BCRA State " + newState.name() + " - but round already began.");
                    return;
                }
            }
            System.out.println("BCRA State " + newState.name());
            switch(newState){
                case BROADCAST:
                    onBroadcast.fire(currentRound);
                    break;
                case CONSOLIDATION:
                    onConsolidation.fire(currentRound);
                    break;
                case RESOLVE:
                    onResolve.fire(currentRound);
                    break;
                case AFFIRM:
                    onAffirm.fire(currentRound);
                    break;
            }
        }
    }

    private static long timeInMinutes(int minutes){
        return (long)(60 * minutes);
    }

    public void putWinner(long round, CommitmentList winner){
        winners.put(round, winner);
    }

    public CommitmentList getCurrentList(){
        return winners.get(getCurrentRound() - 1);
    }

    public static BCRARoundState getCurrentRoundState(){
        long inSeconds = Time.getUTCTimestampInSeconds();
        long timeIntoDuration = inSeconds % BCRA_DURATION_SECONDS;
        if (timeIntoDuration <= timeInMinutes(2))
            return BCRARoundState.BROADCAST;
        else if (timeIntoDuration <= timeInMinutes(3))
            return BCRARoundState.CONSOLIDATION;
        else if (timeIntoDuration <= timeInMinutes(4))
            return BCRARoundState.RESOLVE;
        else
            return BCRARoundState.AFFIRM;
    }

    public long getRoundStakeCutoffTime(long roundNumber){
        return roundNumber * BCRA_DURATION_SECONDS;
    }

    public static long getCurrentRound(){
        long inSeconds = Time.getUTCTimestampInSeconds();
        // Long ALWAYS rounds down.
        return inSeconds/BCRA_DURATION_SECONDS;
    }

    public static long getRound(long seconds){
        return seconds/BCRA_DURATION_SECONDS;
    }

    public static long getExecutorRound(long seconds){
        return getRound(seconds) - 1;
    }

    public static long getTimestampForRound(long round){
        return round * BCRA_DURATION_SECONDS;
    }

    public void transmitList(SignedCommitmentList signedCommitmentList){
        Message message = new BCRAList(signedCommitmentList).toMessage(6);
        node.getRouter().broadcast(message);
    }

    public Router getRouter(){
        return node.getRouter();
    }

    private void commit(ExecutorCommitment executorCommitment){
        node.getRouter().broadcast(
                new BCRACommitment(
                        executorCommitment
                ).toMessage(6)
        );
    }

    private void commitAll(){
        for(ExecutorCommitmentFactory factory : toCommitEachRound){
            commit(factory.toExecutorCommitment(
                    getCurrentRound(),
                    DIFFICULTY_REQUIRED
            ));
        }
    }

    public void loadFromFile(File bcraFile){
        ConfReader.readConfigurationFile(
                bcraFile,
                (index, value) -> {
                    winners.put(Long.valueOf(index),
                            CommitmentList.fromString(
                                    Hex.decodeString(value)
                            ));
                }
        );
    }

    public void addPeriodicSaving(File winnersFile){
        if (!isSavingPeriodically.isRaised()){
            periodicSavingExecutorService.scheduleAtFixedRate(
                    () -> {
                        saveToDisk(winnersFile);
                    },
                    10,
                    10,
                    TimeUnit.SECONDS
            );
            isSavingPeriodically.raise();
        }
    }

    public void saveOnShutdown(File winnersFile){
        if (!isSavingOnShutdown.isRaised()){
            Runtime.getRuntime().addShutdownHook(new Thread(
                    () -> {
                        saveToDisk(winnersFile);
                    }
            ));
            isSavingOnShutdown.raise();
        }
    }

    private void saveToDisk(File file){
        ConfWriter.writeConfigurationFile(file, new BCRAWinnersSerializedProvider(winners));
    }

    private class BCRAWinnersSerializedProvider implements Provider<Dual<String, String>> {

        private final Stack<Dual<String, String>> stack;

        @Override
        public Dual<String, String> provide(){
            return stack.pop();
        }

        @Override
        public boolean exhausted(){
            return stack.size() == 0;
        }

        private BCRAWinnersSerializedProvider(
                Map<Long, CommitmentList> winners
        ){
            this.stack = new Stack<>();
            for (Map.Entry<Long, CommitmentList> entry : winners.entrySet()){
                stack.push(new DualString(
                        entry.getKey().toString(),
                        Hex.encodeString(entry.getValue().toString())
                ));
            }
        }

        private class DualString implements Dual<String, String> {

            private final String a;

            private final String b;

            public DualString(String a, String b){
                this.a = Preconditions.checkNotNull(a);
                this.b = Preconditions.checkNotNull(b);
            }

            @Override
            public String getA() {
                return a;
            }

            @Override
            public String getB() {
                return b;
            }

        }

    }

    public void commitIdentityForEachRound(ExecutorCommitmentFactory factory){
        toCommitEachRound.add(factory);
    }

    public CommitmentList getWinnerForRound(long round){
        return winners.get(round);
    }

    public Event<Long> getOnAffirm() {
        return onAffirm;
    }

    public Event<Long> getOnBroadcast() {
        return onBroadcast;
    }

    public Event<Long> getOnConsolidation() {
        return onConsolidation;
    }

    public Event<Long> getOnResolve() {
        return onResolve;
    }

    public Event<Long> getOnCurrentRoundChange() {
        return onCurrentRoundChange;
    }
}
