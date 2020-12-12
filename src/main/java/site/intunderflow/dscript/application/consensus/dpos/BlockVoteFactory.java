package site.intunderflow.dscript.application.consensus.dpos;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.Block;
import site.intunderflow.dscript.network.message.content.BlockVote;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

public class BlockVoteFactory {

    private final byte[] issue;

    public BlockVoteFactory(byte[] issue){
        this.issue = Preconditions.checkNotNull(issue);
    }

    public BlockVote voteFor(Block block, SignatureFactory signatureFactory) throws GeneralSecurityException {
        BlockVote rawBlockVote = new BlockVote(issue, block);
        return new BlockVote(issue, block,
                signatureFactory.sign(rawBlockVote.getStringToSign().getBytes(StandardCharsets.UTF_8)));
    }

}
