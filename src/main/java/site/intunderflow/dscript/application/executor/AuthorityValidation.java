package site.intunderflow.dscript.application.executor;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.ArrayUtils;
import site.intunderflow.dscript.application.authority.AuthoritySelection;
import site.intunderflow.dscript.application.authority.AuthoritySignatureContent;
import site.intunderflow.dscript.application.bcra.BCRARound;
import site.intunderflow.dscript.application.bcra.commitment.CommitmentList;
import site.intunderflow.dscript.application.bcra.commitment.ExecutorCommitment;
import site.intunderflow.dscript.application.blocklattice.NetworkState;
import site.intunderflow.dscript.application.blocklattice.blockchain.address.BaseAddress;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.DAppStateChangeBlock;
import site.intunderflow.dscript.application.consensus.dpos.Signature;
import site.intunderflow.dscript.utility.ByteBuffer;
import site.intunderflow.dscript.utility.hashing.SHA512;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class AuthorityValidation {

    private final BCRARound bcraRound;

    private final NetworkState networkState;

    public AuthorityValidation(BCRARound bcraRound, NetworkState networkState){
        this.bcraRound = Preconditions.checkNotNull(bcraRound);
        this.networkState = Preconditions.checkNotNull(networkState);
    }

    public void validateAuthority(
            List<Signature> authoritySignatures,
            DAppStateChangeBlock dAppStateChangeBlock
    ) throws GeneralSecurityException  {
        // Determine active BCRA round number from timestap.
        // Is current round - 1 (as in the last completed round).
        long round = BCRARound.getExecutorRound(dAppStateChangeBlock.getUTCTimestampInSeconds());
        // Get authority for the round.
        CommitmentList commitmentList = bcraRound.getWinnerForRound(round);
        Preconditions.checkState(commitmentList != null, "Unknown BCRA list for round " + round);
        AuthoritySelection selector = new AuthoritySelection(
                commitmentList,
                networkState,
                round
        );
        List<ExecutorCommitment> authorityCommitments = selector.selectAuthority();
        List<BaseAddress> authority = new ArrayList<>();
        for (ExecutorCommitment commitment : authorityCommitments){
            authority.add(commitment.getExecutor().getBaseAddress());
        }
        byte[] toSign = AuthoritySignatureContent.getContentToSign(
                round,dAppStateChangeBlock.getPreviousReference(), dAppStateChangeBlock.getPermanentMemory()
        );
        // First we validate that the authorities are genuine.
        Preconditions.checkArgument(authoritySignatures.size() == authority.size(),
                "Size mismatch, actual authority size: " + authority.size()
                        + " size presented: " + authoritySignatures.size());
        for (BaseAddress authorityMember : authority){
            Preconditions.checkArgument(containsAddress(authoritySignatures, authorityMember),
                    "Authority " + authorityMember.getAddress() + " not found in signatures.");
        }
        for (Signature signature : authoritySignatures){
            // Validate the presented signature.
            signature.getPublicKey(networkState).verifySignatureViaExceptionThrow(
                    signature.getSignature(), toSign
            );
        }
    }

    private boolean containsAddress(List<Signature> signatures, BaseAddress address){
        for (Signature signature : signatures){
            if (signature.getAddress().equals(address)){
                return true;
            }
        }
        return false;
    }

}
