package site.intunderflow.dscript.application.blocklattice;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.application.blocklattice.blockchain.Blockchain;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.Block;
import site.intunderflow.dscript.utility.ThreadSafeDataPassing;
import site.intunderflow.dscript.utility.ThreadSafeSummation;

import java.security.GeneralSecurityException;
import java.util.Stack;
import java.util.function.Consumer;

/** Allows interaction with a blockchain to get the account value, validate transactions, etc. */
public class BlockchainInterface {

    private final Blockchain blockchain;

    private final NetworkState networkState;

    public BlockchainInterface(
            Blockchain blockchain,
            NetworkState networkState
    ){
        this.blockchain = Preconditions.checkNotNull(blockchain);
        this.networkState = Preconditions.checkNotNull(networkState);
    }

    public long getAccountConfirmedValueAtTime(long time){
        try{
            checkChainValid();
        }
        catch (GeneralSecurityException e){
            return 0;
        }
        return getAccountConfirmedValueAtTimeNoCheck(time);
    }

    public long getAccountConfirmedValueAtTimeNoCheck(long time){
        ThreadSafeSummation summation = new ThreadSafeSummation();
        new BlockchainTraceback(networkState, (block) -> {
            if (block.getUTCTimestampInSeconds() <= time){
                summation.add(BlockValueEvaluator.getValue(block));
            }
        }).traceConfirmedOnly(blockchain.getHead());
        return summation.sum();
    }

    public long getAccountValue(){
        try{
            checkChainValid();
        }
        catch (GeneralSecurityException e){
            return 0;
        }
        return getAccountValueNoCheck();
    }

    public long getConfirmedAccountValue(){
        try{
            getConfirmedChainValid();
        }
        catch(GeneralSecurityException e){
            return 0;
        }
        return getAccountConfirmedValueNoCheck();
    }

    public long getAccountConfirmedValueNoCheck(){
        ThreadSafeSummation summation = new ThreadSafeSummation();
        new BlockchainTraceback(networkState, (block) -> {
            summation.add(BlockValueEvaluator.getValue(block));
        }).traceConfirmedOnly(blockchain.getHead());
        return summation.sum();
    }

    public long getAccountValueNoCheck(){
        ThreadSafeSummation summation = new ThreadSafeSummation();
        new BlockchainTraceback(networkState, (block) -> {
            summation.add(BlockValueEvaluator.getValue(block));
        }).trace(blockchain.getHead());
        return summation.sum();
    }

    public void getConfirmedChainValid() throws GeneralSecurityException {
        Stack<Block> blocks = new Stack<>();
        new BlockchainTraceback(networkState, blocks::push).traceConfirmedOnly(blockchain.getHead());
        checkBlocks(blocks);
    }

    public void checkChainValid() throws GeneralSecurityException {
        Stack<Block> blocks = new Stack<>();
        new BlockchainTraceback(networkState, blocks::push).trace(blockchain.getHead());
        checkBlocks(blocks);
    }

    private void checkBlocks(Stack<Block> blocks) throws GeneralSecurityException{
        boolean firstBlock = true;
        BlockValidityChecker.Context context = null;
        while (!blocks.empty()){
            if (firstBlock){
                firstBlock = false;
                context = new BlockValidityChecker(networkState).checkBlockValid(blocks.pop());
            }
            else{
                context = new BlockValidityChecker(networkState).checkBlockValid(blocks.pop(), context);
            }
        }
    }

}
