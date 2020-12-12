package site.intunderflow.dscript.application.authority;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.application.bcra.BCRARound;
import site.intunderflow.dscript.application.bcra.commitment.CommitmentList;
import site.intunderflow.dscript.application.bcra.commitment.ExecutorCommitment;
import site.intunderflow.dscript.application.bcra.seeding.BCRASeeder;
import site.intunderflow.dscript.application.blocklattice.BlockchainInterface;
import site.intunderflow.dscript.application.blocklattice.NetworkState;
import site.intunderflow.dscript.utility.Dual;
import site.intunderflow.dscript.utility.crypto.prng.PRNG;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Selects the Authority, as in the Random Committee.
 */
public class AuthoritySelection {

    private final long rangeMax;

    private final LinkedList<Dual<ExecutorCommitment, Long>> executorsRange = new LinkedList<>();

    private final PRNG prng;

    public AuthoritySelection(
            CommitmentList bcraList,
            NetworkState networkState,
            long roundNumber
    ){
        byte[] seed = new BCRASeeder(bcraList).getSeed();
        this.prng = new PRNG(seed);
        long roundStakeCutoffTimestamp = getStartTimeOfRound(roundNumber);
        long max = 0;
        for (ExecutorCommitment executorCommitment : bcraList.getCommitments()){
            max = max + getStake(
                    executorCommitment,
                    networkState,
                    roundStakeCutoffTimestamp
            );
            executorsRange.add(
                    new ExecutorWithMax(
                            executorCommitment,
                            max
                    )
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

    public ExecutorCommitment select(){
        long position = selectPositionAlongRange();
        if (position == 0){
            return executorsRange.get(0).getA();
        }
        long previousMax = 0;
        for (int i = 0; i < executorsRange.size(); i++){
            if (executorsRange.get(i).getB() <= position && previousMax < position){
                return executorsRange.get(i).getA();
            }
            previousMax = executorsRange.get(i).getB();
        }
        throw new IllegalStateException("Unable to find the position on the range: " + position);
    }

    public List<ExecutorCommitment> selectAuthority(){
        List<ExecutorCommitment> authority = new ArrayList<>();
        for (int i = 0; i < 10; i++){
            ExecutorCommitment member = select();
            if (!authority.contains(member)){
                authority.add(member);
            }
        }
        return authority;
    }

    private long selectPositionAlongRange(){
        long randomLong = prng.nextLong();
        if (randomLong < 0){
            randomLong = randomLong * -1;
        }
        // Bind to the range.
        randomLong = randomLong * (rangeMax / Long.MAX_VALUE);
        return randomLong;
    }

    private class ExecutorWithMax implements Dual<ExecutorCommitment, Long>{

        private final ExecutorCommitment a;

        private final Long b;

        private ExecutorWithMax(ExecutorCommitment a, long b){
            this.a = Preconditions.checkNotNull(a);
            this.b = Preconditions.checkNotNull(b);
        }

        public ExecutorCommitment getA(){
            return a;
        }

        @Override
        public Long getB() {
            return b;
        }
    }

}
