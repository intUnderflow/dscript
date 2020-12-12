package site.intunderflow.dscript.application.blocklattice.blockchain;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.Block;
import site.intunderflow.dscript.utility.hashing.SHA512;

public class BlockHashFactory {

    private final Block block;

    public BlockHashFactory(Block block){
        this.block = Preconditions.checkNotNull(block);
    }

    public byte[] hash(){
        return SHA512.hash(block.toBytes());
    }

}
