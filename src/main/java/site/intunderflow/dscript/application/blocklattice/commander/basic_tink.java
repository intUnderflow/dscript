package site.intunderflow.dscript.application.blocklattice.commander;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.application.blocklattice.BlockchainInterface;
import site.intunderflow.dscript.application.blocklattice.NetworkState;
import site.intunderflow.dscript.application.blocklattice.blockchain.address.BaseAddress;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.TransferBlock;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.basic_tink.Transfer;
import site.intunderflow.dscript.application.consensus.dpos.Signature;
import site.intunderflow.dscript.application.consensus.dpos.SignatureFactory;

import java.security.GeneralSecurityException;

public class basic_tink implements Commander {

    private final SignatureFactory signatureFactory;

    private final BaseAddress baseAddress;

    private final NetworkState networkState;

    public basic_tink(SignatureFactory signatureFactory,
                      BaseAddress baseAddress,
                      NetworkState networkState){
        this.signatureFactory = Preconditions.checkNotNull(signatureFactory);
        this.baseAddress = Preconditions.checkNotNull(baseAddress);
        this.networkState = Preconditions.checkNotNull(networkState);
    }

    private byte[] getHead(){
        return networkState.getHead(baseAddress);
    }

    @Override
    public BaseAddress getFor(){
        return baseAddress;
    }

    @Override
    public TransferBlock createTransfer(BaseAddress to, long amount) throws GeneralSecurityException {
        Transfer transfer = new Transfer(getHead(), to, amount);
        transfer.signWith(signatureFactory);
        return transfer;
    }

    @Override
    public long getBalance(){
        return new BlockchainInterface(networkState.getBlockchain(baseAddress), networkState)
                .getConfirmedAccountValue();
    }
}
