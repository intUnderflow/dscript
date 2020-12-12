package site.intunderflow.dscript.application.blocklattice.blockchain;

import com.google.common.base.Preconditions;

import java.nio.charset.Charset;

public class BlockStringFromBytesFactory {

    private static final Charset CHARSET = BlockBytesFromStringFactory.CHARSET;

    private final byte[] blockBytes;

    public BlockStringFromBytesFactory(byte[] blockBytes){
        this.blockBytes = Preconditions.checkNotNull(blockBytes);
    }

    public String getString(){
        return new String(blockBytes, CHARSET);
    }

    @Override
    public String toString(){
        return getString();
    }
}
