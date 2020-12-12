package site.intunderflow.dscript.application.blocklattice.blockchain.block;

public abstract class Block {

    public abstract byte[] toBytes();

    public abstract byte[] getPreviousReference();

    public abstract boolean isGenesis();

    public abstract String getType();

    public abstract String getAccountType();

    public abstract long getUTCTimestampInSeconds();

    @Override
    public boolean equals(Object o){
        if (o == null){
            return false;
        }
        if (!o.getClass().equals(getClass())){
            return false;
        }
        Block block = (Block) o;
        return block.toString().equals(toString());
    }
}
