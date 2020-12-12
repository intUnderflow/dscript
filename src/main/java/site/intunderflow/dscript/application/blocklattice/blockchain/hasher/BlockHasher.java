package site.intunderflow.dscript.application.blocklattice.blockchain.hasher;

import site.intunderflow.dscript.application.blocklattice.blockchain.block.Block;
import site.intunderflow.dscript.utility.hashing.SHA512;

public class BlockHasher {

    public static byte[] hash(Block block){
        return SHA512.hash(block.toBytes());
    }

}
