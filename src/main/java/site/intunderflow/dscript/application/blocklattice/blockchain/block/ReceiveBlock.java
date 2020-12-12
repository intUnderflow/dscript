package site.intunderflow.dscript.application.blocklattice.blockchain.block;

public abstract class ReceiveBlock extends Block{

    public abstract byte[] getTransferBlock();

    public abstract long getAmountReceived();

}
