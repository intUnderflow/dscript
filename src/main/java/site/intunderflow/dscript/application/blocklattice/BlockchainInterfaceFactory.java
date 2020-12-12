package site.intunderflow.dscript.application.blocklattice;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.application.blocklattice.blockchain.Blockchain;

public class BlockchainInterfaceFactory {

    private final NetworkState networkState;

    public BlockchainInterfaceFactory(NetworkState networkState){
        this.networkState = Preconditions.checkNotNull(networkState);
    }

    public BlockchainInterface getInterface(Blockchain blockchain){
        return new BlockchainInterface(blockchain, networkState);
    }

}
