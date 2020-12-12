package site.intunderflow.dscript.application.blocklattice.blockchain.block;

import site.intunderflow.dscript.application.blocklattice.blockchain.address.BaseAddress;

public abstract class TransferBlock extends Block {

    public abstract BaseAddress getAddressTo();

    public abstract long getAmount();

    public abstract String getAuthorizationType();

    public abstract byte[] getAuthorization();

    public abstract String getTransactionComponent();

}
