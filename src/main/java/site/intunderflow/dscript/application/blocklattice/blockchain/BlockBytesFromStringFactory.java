package site.intunderflow.dscript.application.blocklattice.blockchain;

import com.google.common.base.Preconditions;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class BlockBytesFromStringFactory {

    static final Charset CHARSET = StandardCharsets.UTF_8;

    private final String blockString;

    public BlockBytesFromStringFactory(
            String blockString
    ){
        this.blockString = Preconditions.checkNotNull(blockString);
    }

    public byte[] getBytes(){
        return blockString.getBytes(CHARSET);
    }

}
