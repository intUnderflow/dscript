package site.intunderflow.dscript.application.authority;

import org.apache.commons.lang3.ArrayUtils;
import site.intunderflow.dscript.application.blocklattice.blockchain.BlockBytesFromStringFactory;
import site.intunderflow.dscript.application.executor.ddl.v1.memory.ByteValue64;
import site.intunderflow.dscript.utility.ByteBuffer;
import site.intunderflow.dscript.utility.JSONUtils;
import site.intunderflow.dscript.utility.hashing.SHA512;

import java.util.Map;

public class AuthoritySignatureContent {

    public static byte[] getContentToSign(
            long currentRound,
            byte[] previousBlock,
            Map<ByteValue64, ByteValue64> memory
    ){
        System.out.println("ROUND " + currentRound);
        byte[] roundBytes = ByteBuffer.allocate(16).putLong(currentRound).array();
        byte[] memoryBytes = new BlockBytesFromStringFactory(
                JSONUtils.permanentMemoryToJSON(
                        memory
                ).toString()
        ).getBytes();
        byte[] roundAndPrevious = ArrayUtils.addAll(
                roundBytes,
                previousBlock
        );
        return SHA512.hash(
                ArrayUtils.addAll(
                        roundAndPrevious,
                        memoryBytes
                )
        );
    }

}
