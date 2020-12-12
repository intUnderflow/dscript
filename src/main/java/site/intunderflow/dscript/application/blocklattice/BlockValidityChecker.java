package site.intunderflow.dscript.application.blocklattice;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import site.intunderflow.dscript.application.blocklattice.blockchain.BlockBytesFromStringFactory;
import site.intunderflow.dscript.application.blocklattice.blockchain.address.BaseAddress;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.Block;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.CreateBlock;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.ReceiveBlock;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.TransferBlock;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.dapp_ddl.DAppCreate;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.dapp_ddl.StateChanged;
import site.intunderflow.dscript.application.consensus.dpos.Signature;
import site.intunderflow.dscript.application.executor.AuthorityValidation;
import site.intunderflow.dscript.authorization.AuthorizationCheck;
import site.intunderflow.dscript.utility.ComparableByteArray;
import site.intunderflow.dscript.utility.Immutables;
import site.intunderflow.dscript.utility.Time;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;


public class BlockValidityChecker {

    private final NetworkState networkState;

    public BlockValidityChecker(NetworkState networkState){
        this.networkState = Preconditions.checkNotNull(networkState);
    }

    public boolean checkBlockValidNoException(Block block){
        try{
            checkBlockValid(block);
        }
        catch(GeneralSecurityException e){
            return false;
        }
        return true;
    }

    public Context checkBlockValid(Block block) throws GeneralSecurityException {
        return checkBlockValid(block, Context.getDefault());
    }

    public Context checkBlockValid(Block block, Context context) throws GeneralSecurityException {
        if (block.getUTCTimestampInSeconds() > Time.getUTCTimestampInSeconds()){
            throw new GeneralSecurityException(
                    "Timestamp on the block is in the future"
            );
        }
        else if (block.isGenesis() || block.getType().equals("create")){
            if (context.isInitialized()){
                throw new GeneralSecurityException(
                        "A genesis block was discovered but the context says this account has been initialized!"
                );
            }
            if (block.getType().equals("dapp_create")){
                CreateBlock createBlock = (CreateBlock) block;
                return new Context(0, true, block.getAccountType(),
                        createBlock.getInitializationParams(),
                        BaseAddress.forCreateBlock(createBlock), context.getClaimedTransfers());
            }
            else{
                if (block.getType().equals("name_registration")) {
                    return Context.getDefault();
                }
                else {
                    CreateBlock createBlock = (CreateBlock) block;
                    String accountType = createBlock.getAccountType();
                    long createdWithValue = BlockValueEvaluator.getValue(block);
                    return new Context(createdWithValue, true, accountType, createBlock.getInitializationParams(),
                            BaseAddress.forCreateBlock(createBlock), context.getClaimedTransfers());
                }
            }
        }
        else if (!context.isInitialized()){
            throw new GeneralSecurityException(
                    "The account is not initialized but the block requires initialization!"
            );
        }
        else if (block.getType().equals("dapp_state_changed")){
            StateChanged stateChangedBlock = (StateChanged) block;
            Immutables<Signature> immutablesUtil = new Immutables<>();
            new AuthorityValidation(
                    networkState.getNode().getBcraRound(),
                    networkState
            ).validateAuthority(
                    immutablesUtil.cloneToMutableList(
                            stateChangedBlock.getAuthoritySignatures()
                    ),
                    stateChangedBlock
            );
            return context;
        }
        else if (block.getType().equals("transfer")){
            TransferBlock transferBlock = (TransferBlock) block;
            String authorizationType = transferBlock.getAuthorizationType();
            byte[] authorization = transferBlock.getAuthorization();
            // Check authorization.
            new AuthorizationCheck(context.getInitializationParams()).checkAuthorization(
                    authorizationType,
                    authorization,
                    new BlockBytesFromStringFactory(
                            transferBlock.getTransactionComponent()
                    ).getBytes()
            );
            long accountValue = context.getAccountValue();
            long transferAmount = transferBlock.getAmount();
            if (transferAmount > accountValue){
                throw new GeneralSecurityException(
                        "The transfer is too big! Account value: "
                                + accountValue
                                + ", Transfer amount: "
                                + transferAmount
                );
            }
            else{
                accountValue = accountValue - transferAmount;
                return new Context(
                        accountValue,
                        context.isInitialized(),
                        context.getAccountType(),
                        context.getInitializationParams(),
                        context.getAddress(),
                        context.getClaimedTransfers()
                );
            }
        }
        else if (block.getType().equals("receive")){
            ReceiveBlock receiveBlock = (ReceiveBlock) block;
            if (context.getClaimedTransfers().contains(new ComparableByteArray(receiveBlock.getTransferBlock()))){
                throw new GeneralSecurityException(
                        "Transfer has already been claimed on chain!"
                );
            }
            TransferBlock transferBlock = (TransferBlock) networkState.getLocally(receiveBlock.getTransferBlock());
            // No need to verify transfer because accepted locally.
            if (transferBlock == null){
                throw new GeneralSecurityException(
                        "We could not accept the transfer because the transfer block is not confirmed."
                );
            }
            if (!transferBlock.getAddressTo().equals(context.getAddress())){
                throw new GeneralSecurityException(
                        "The transfer isn't destined for this account!"
                );
            }
            if (transferBlock.getAmount() != receiveBlock.getAmountReceived()){
                throw new GeneralSecurityException(
                        "Amount input isn't amount output!"
                );
            }
            List<ComparableByteArray> claimed = context.getClaimedTransfers();
            claimed.add(new ComparableByteArray(receiveBlock.getTransferBlock()));
            return new Context(
                    context.getAccountValue() + receiveBlock.getAmountReceived(),
                    context.isInitialized(),
                    context.getAccountType(),
                    context.getInitializationParams(),
                    context.getAddress(),
                    claimed
            );
        }
        throw new GeneralSecurityException(
                "Illegal state! Block not recognized!"
        );
    }

    public static class Context {

        private final long accountValue;
        private final boolean initialized;
        private final String accountType;
        private final ImmutableMap<String, Object> initializationParams;
        private final BaseAddress address;
        private final List<ComparableByteArray> claimedTransfers;

        private Context(
                long accountValue,
                boolean initialized,
                String accountType,
                ImmutableMap<String, Object> initializationParams,
                BaseAddress address,
                List<ComparableByteArray> claimedTransfers
        ){
            this.accountValue = accountValue;
            this.initialized = initialized;
            this.accountType = accountType;
            this.initializationParams = initializationParams;
            this.address = address;
            this.claimedTransfers = claimedTransfers;
        }

        private long getAccountValue(){
            return accountValue;
        }

        private boolean isInitialized(){
            return initialized;
        }

        private String getAccountType(){
            return accountType;
        }

        private ImmutableMap<String, Object> getInitializationParams() {
            return initializationParams;
        }

        private BaseAddress getAddress(){
            return address;
        }

        private List<ComparableByteArray> getClaimedTransfers(){ return claimedTransfers; }

        private static Context getDefault(){
            return
                    new Context(
                            0,
                            false,
                            null,
                            null,
                            null,
                            new ArrayList<>()
                    );
        }

    }

}
