package site.intunderflow.dscript.application.executor;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.application.authority.AuthoritySignatureContent;
import site.intunderflow.dscript.application.bcra.BCRARound;
import site.intunderflow.dscript.application.bcra.commitment.CommitmentList;
import site.intunderflow.dscript.application.bcra.commitment.ExecutorCommitment;
import site.intunderflow.dscript.application.blocklattice.NetworkState;
import site.intunderflow.dscript.application.blocklattice.blockchain.BlockBytesFromStringFactory;
import site.intunderflow.dscript.application.blocklattice.blockchain.address.BaseAddress;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.Block;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.DAppStateChangeBlock;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.dapp_ddl.DAppCreate;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.dapp_ddl.StateChanged;
import site.intunderflow.dscript.application.consensus.dpos.Signature;
import site.intunderflow.dscript.application.consensus.dpos.SignatureFactory;
import site.intunderflow.dscript.application.executor.ddl.v1.executor.InstructionExecutor;
import site.intunderflow.dscript.application.executor.ddl.v1.memory.ByteValue64;
import site.intunderflow.dscript.application.executor.ddl.v1.memory.ProgramState;
import site.intunderflow.dscript.application.executor.ddl.v1.program.InstructionSet;
import site.intunderflow.dscript.application.executor.ddl.v1.utility.FinishStateCodes;
import site.intunderflow.dscript.application.lddb.database.Database;
import site.intunderflow.dscript.network.message.Message;
import site.intunderflow.dscript.network.message.MessageContent;
import site.intunderflow.dscript.network.message.Router;
import site.intunderflow.dscript.network.message.content.ExecutionRequest;
import site.intunderflow.dscript.network.message.content.ExecutorConfirmation;
import site.intunderflow.dscript.thirdparty.SelfExpiringHashMap;
import site.intunderflow.dscript.utility.*;
import site.intunderflow.dscript.utility.hashing.SHA512;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BackgroundService {

    private final Router router;

    private final BCRARound bcraRound;

    private final SignatureFactory signatureFactory;

    private final NetworkState networkState;

    private final Database lddb;

    private final SelfExpiringHashMap<ComparableByteArray, List<Signature>> authorityAgreements;

    private long currentRound;

    private List<BaseAddress> currentAuthority;

    public BackgroundService(Router router,
                             BCRARound bcraRound,
                             NetworkState networkState,
                             Database lddb,
                             SignatureFactory signatureFactory){
        this.router = Preconditions.checkNotNull(router);
        this.bcraRound = Preconditions.checkNotNull(bcraRound);
        this.networkState = Preconditions.checkNotNull(networkState);
        this.lddb = Preconditions.checkNotNull(lddb);
        this.signatureFactory = Preconditions.checkNotNull(signatureFactory);
        this.authorityAgreements = new SelfExpiringHashMap<>(300 * 1000);
        this.currentAuthority = new ArrayList<>();
        this.currentRound = 0;
        bcraRound.getOnCurrentRoundChange().subscribeRunnable(this::resetMapAgreement);
        bcraRound.getOnCurrentRoundChange().subscribeRunnable(this::getCurrentAuthority);
        bcraRound.getOnCurrentRoundChange().subscribe((round) -> {
            currentRound = round;
        });
        router.onMessage(this::onMessage);
    }

    private void resetMapAgreement(){
        authorityAgreements.clear();
    }

    private void getCurrentAuthority(){
        long executorRound = BCRARound.getExecutorRound(Time.getUTCTimestampInSeconds());
        CommitmentList commitmentList = bcraRound.getWinnerForRound(executorRound);
        if (commitmentList == null){
            return;
        }
        List<BaseAddress> newAuthority = new ArrayList<>();
        for (ExecutorCommitment commitment : commitmentList.getCommitments()){
            newAuthority.add(commitment.getExecutor().getBaseAddress());
        }
        this.currentAuthority = newAuthority;
        System.out.println("THE CURRENT AUTHORITY IS:");
        for (BaseAddress address : currentAuthority){
            System.out.println(address);
        }
    }

    private void onMessage(Message message){
        MessageContent messageContent = message.getContent();
        if (messageContent.getType().equals("ExecutionRequest")){
            onExecutionRequest((ExecutionRequest) messageContent);
        }
        else if (messageContent.getType().equals("ExecutorConfirmation")){
            onExecutorConfirmation((ExecutorConfirmation) messageContent);
        }
    }

    private void onExecutorConfirmation(ExecutorConfirmation confirmation){
        if (currentRound == 0){
            return; // Not ready.
        }
        // Check the Signature is on the current authorities.
        if (!currentAuthority.contains(
                confirmation.getSignature().getAddress()
        )){
            return;
        }
        // Validate the signature.
        if (!confirmation.getSignature().getPublicKey(networkState).verifySignature(
                confirmation.getSignature().getSignature(),
                AuthoritySignatureContent.getContentToSign(
                        currentRound - 1,
                        confirmation.getStateChangeBlock().getPreviousReference(),
                        confirmation.getStateChangeBlock().getPermanentMemory()
                )
        )){
            return;
        }
        ComparableByteArray subject = new ComparableByteArray(SHA512.hash(
                new BlockBytesFromStringFactory(
                        confirmation.getStateChangeBlock().toString()
                ).getBytes()
        ));
        List<Signature> agreement;
        if (authorityAgreements.containsKey(subject)){
            agreement = authorityAgreements.get(subject);
        }
        else{
            agreement = new ArrayList<>();
        }
        if (agreement.contains(confirmation.getSignature())){
            return;
        }
        agreement.add(confirmation.getSignature());
        authorityAgreements.put(subject, agreement);
        if (agreement.size() == currentAuthority.size()){
            // The authority have agreed! Publish the Block!
            StateChanged stateChanged = new StateChanged(
                    confirmation.getStateChangeBlock().getPreviousReference(),
                    new ArrayList<>(agreement),
                    confirmation.getStateChangeBlock().getPermanentMemory(),
                    BCRARound.getTimestampForRound(currentRound)
            );
            lddb.broadcastNewData(stateChanged.toBytes());
        }
    }

    @SuppressWarnings("unchecked")
    private void onExecutionRequest(ExecutionRequest executionRequest){
        long currentRound = BCRARound.getExecutorRound(
                Time.getUTCTimestampInSeconds()
        );
        if (amIACurrentExecutor(currentRound)){
            // Look up the DApp initialization information.
            Block createBlockRaw = networkState.getGenesis(executionRequest.getDappAddress());
            if (createBlockRaw == null){
                return;
            }
            DAppCreate createBlock = (DAppCreate) createBlockRaw;
            InstructionSet sourceCode = InstructionSet.fromBytes(
                    Hex.decode((String) createBlock.getInitializationParams().get("source")));
            byte[] headBlockHash = networkState.getHead(executionRequest.getDappAddress());
            if (headBlockHash == null){
                return;
            }
            Block headBlock = networkState.get(headBlockHash);
            if (headBlock == null){
                return;
            }
            Map<ByteValue64, ByteValue64> memory = new HashMap<>();
            if (headBlock.isGenesis()){
                Object memoryObject = createBlock.getInitializationParams().get("permanentMemory");
                memory = (Map<ByteValue64, ByteValue64>) memoryObject;
            }
            else{
                DAppStateChangeBlock changeBlock = (DAppStateChangeBlock) headBlock;
                for (Map.Entry<ByteValue64, ByteValue64> entry : changeBlock.getPermanentMemory().entrySet()){
                    memory.put(entry.getKey(), entry.getValue());
                }
            }
            ProgramState programState = new ProgramState(
                    executionRequest.getInput(),
                    sourceCode
            );
            for (Map.Entry<ByteValue64, ByteValue64> entry : memory.entrySet()){
                programState.addToPermanentMemory(entry.getKey(), entry.getValue());
            }
            InstructionExecutor instructionExecutor = new InstructionExecutor(
                    programState
            );
            instructionExecutor.executeProgram();
            if (programState.getFinishStateCode() != FinishStateCodes.SUCCESS){
                System.out.println("Execution finished with error code " + programState.getFinishStateCode());
                return;
            }
            Map<ByteValue64, ByteValue64> resultingMemory = programState.getPermanentMemory();
            if (resultingMemory.equals(memory)){
                return; // Nothing changed.
            }
            byte[] toSign = AuthoritySignatureContent.getContentToSign(
                    currentRound,
                    headBlockHash,
                    resultingMemory
            );
            Signature signature;
            try {
                signature = signatureFactory.sign(toSign);
            }
            catch(GeneralSecurityException e){
                throw new Error(e);
            }
            StateChanged stateChanged = new StateChanged(
                    headBlockHash,
                    new ArrayList<>(),
                    resultingMemory,
                    Time.getUTCTimestampInSeconds()
            );
            ExecutorConfirmation executorConfirmation = new ExecutorConfirmation(
                    stateChanged,
                    signature
            );
            router.broadcast(executorConfirmation.toMessage(6));
        }
    }

    public boolean amIACurrentExecutor(long currentRound){
        CommitmentList commitmentList = bcraRound.getWinnerForRound(
                currentRound
        );
        if (commitmentList == null){
            return false;
        }
        else{
            for(ExecutorCommitment executorCommitment : commitmentList.getCommitments()){
                if (executorCommitment.getExecutor().getBaseAddress().equals(
                        signatureFactory.getBaseAddress()
                )){
                    return true;
                }
            }
        }
        return false;
    }

}
