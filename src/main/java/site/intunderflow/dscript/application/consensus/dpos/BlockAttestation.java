package site.intunderflow.dscript.application.consensus.dpos;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.application.blocklattice.BlockchainInterface;
import site.intunderflow.dscript.application.blocklattice.BlockchainInterfaceFactory;
import site.intunderflow.dscript.application.blocklattice.NetworkState;
import site.intunderflow.dscript.application.blocklattice.blockchain.Blockchain;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.Block;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.CreateBlock;
import site.intunderflow.dscript.utility.crypto.keys.KeyPair;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

/**
 * An attestation is a block and a collection of signatures of representatives to that block.
 */
public class BlockAttestation {

    private final Block block;

    private final List<Signature> signatureList;

    public BlockAttestation(Block block){
        this.block = Preconditions.checkNotNull(block);
        this.signatureList = new ArrayList<>();
    }

    public void sign(KeyPair keyPair, CreateBlock createBlock) throws GeneralSecurityException {
        Preconditions.checkArgument(
                keyPair.getPublicKey().equals(
                        createBlock.getInitializationParams().get("publicKey")
                )
        );
    }

    public void addSignature(Signature signature){
        if (!signatureList.contains(signature)){
            signatureList.add(signature);
        }
    }

    public Block getBlock() {
        return block;
    }

    public long getAttestationConfirmedWeight(NetworkState networkState){
        long confirmedWeight = 0;
        for (Signature signature : signatureList){
            Blockchain blockchain = networkState.getBlockchain(signature.getAddress());
            BlockchainInterface blockchainInterface = new BlockchainInterfaceFactory(networkState)
                    .getInterface(blockchain);
            long value = blockchainInterface.getConfirmedAccountValue();
            confirmedWeight = confirmedWeight + value;
        }
        return confirmedWeight;
    }

}
