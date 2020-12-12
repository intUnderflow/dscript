package site.intunderflow.dscript.application.authorityselection;

import site.intunderflow.dscript.application.bcra.BCRARound;
import site.intunderflow.dscript.application.bcra.commitment.CommitmentList;
import site.intunderflow.dscript.application.bcra.commitment.ExecutorCommitment;
import site.intunderflow.dscript.application.bcra.seeding.BCRASeeder;
import site.intunderflow.dscript.application.blocklattice.BlockchainInterface;
import site.intunderflow.dscript.application.blocklattice.NetworkState;
import site.intunderflow.dscript.utility.crypto.prng.PRNG;

/**
 * Selects the Authority, as in the Random Committee.
 */
public class AuthoritySelection {

    private final long rangeMax;

    public AuthoritySelection(
            CommitmentList bcraList,
            NetworkState networkState,
            long roundNumber
    ){
        byte[] seed = new BCRASeeder(bcraList).getSeed();
        PRNG prng = new PRNG(seed);
        long roundStakeCutoffTimestamp = getStartTimeOfRound(roundNumber);
        long max = 0;
        for (ExecutorCommitment executorCommitment : bcraList.getCommitments()){
            max = max + getStake(
                    executorCommitment,
                    networkState,
                    roundStakeCutoffTimestamp
            );
        }
        rangeMax = max;
    }

    private long getStake(ExecutorCommitment commitment, NetworkState networkState, long atTime){
        return new BlockchainInterface(
                networkState.getBlockchain(
                        commitment.getExecutor().getBaseAddress()
                ),
                networkState
        ).getAccountConfirmedValueAtTime(atTime);
    }

    private long getStartTimeOfRound(long round){
        return (round * BCRARound.BCRA_DURATION_SECONDS);
    }

}
