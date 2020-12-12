package site.intunderflow.dscript.application.blocklattice;

import site.intunderflow.dscript.application.GenesisAccounts;
import site.intunderflow.dscript.application.blocklattice.blockchain.BlockHashFactory;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.Block;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.ReceiveBlock;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.TransferBlock;

public class BlockValueEvaluator {

    public static long getValue(Block block){
        if (block.isGenesis()){
            byte[] hash = new BlockHashFactory(block).hash();
            return GenesisAccounts.getGenesisAmount(hash);
        }
        else if (block.getType().equals("transfer")){
            TransferBlock transferBlock = (TransferBlock) block;
            return -transferBlock.getAmount();
        }
        else if (block.getType().equals("receive")){
            ReceiveBlock receiveBlock = (ReceiveBlock) block;
            return receiveBlock.getAmountReceived();
        }
        // Default.
        return 0;
    }

}
