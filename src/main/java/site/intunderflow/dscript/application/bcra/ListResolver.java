package site.intunderflow.dscript.application.bcra;

import com.google.common.base.Preconditions;
import org.checkerframework.checker.signature.qual.PolySignature;
import site.intunderflow.dscript.application.bcra.commitment.ExecutorCommitment;
import site.intunderflow.dscript.application.bcra.commitment.SignedCommitmentList;
import site.intunderflow.dscript.application.blocklattice.BlockchainInterface;
import site.intunderflow.dscript.application.blocklattice.NetworkState;

import javax.annotation.Signed;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListResolver {

    private final NetworkState networkState;

    private final List<SignedCommitmentList> signedCommitmentLists;

    private final Map<ExecutorCommitment, Long> weights;

    public ListResolver(NetworkState networkState){
        this.networkState = Preconditions.checkNotNull(networkState);
        this.signedCommitmentLists = new ArrayList<>();
        this.weights = new HashMap<>();
    }

    public void add(SignedCommitmentList list){
        long weightOfAuthor = new BlockchainInterface(
                networkState.getBlockchain(list.getSignature().getAddress()), networkState
        ).getConfirmedAccountValue();
        for (ExecutorCommitment executorCommitment : list.getList().getCommitments()){
            long newWeight = weights.getOrDefault(executorCommitment, (long)0) + weightOfAuthor;
            weights.put(executorCommitment, newWeight);
        }
        signedCommitmentLists.add(list);
    }

    private long getWeight(SignedCommitmentList list){
        long totalWeight = 0;
        for (ExecutorCommitment executorCommitment : list.getList().getCommitments()){
            totalWeight = totalWeight + weights.getOrDefault(executorCommitment, (long)0);
        }
        return totalWeight;
    }

    public SignedCommitmentList getWinner(){
        long winnerWeight = -1;
        SignedCommitmentList winner = null;
        for (SignedCommitmentList list : signedCommitmentLists){
            long weight = getWeight(list);
            if (weight > winnerWeight){
                winnerWeight = weight;
                winner = list;
            }
        }
        return winner;
    }

}
