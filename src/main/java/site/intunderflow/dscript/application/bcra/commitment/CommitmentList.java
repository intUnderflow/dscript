package site.intunderflow.dscript.application.bcra.commitment;

import com.google.common.base.Preconditions;
import org.json.JSONArray;
import site.intunderflow.dscript.utility.hashing.SHA512;

import java.util.ArrayList;
import java.util.List;

public class CommitmentList {

    private final List<ExecutorCommitment> commitments;

    public CommitmentList(){
        commitments = new ArrayList<>();
    }

    public CommitmentList(List<ExecutorCommitment> list){
        commitments = Preconditions.checkNotNull(list);
    }

    public List<ExecutorCommitment> getCommitments() {
        return new ArrayList<>(commitments);
    }

    public void add(ExecutorCommitment commitment){
        if (!commitments.contains(commitment)) {
            commitments.add(commitment);
        }
    }

    @Override
    public String toString(){
        JSONArray jsonArray = new JSONArray();
        for(ExecutorCommitment commitment : commitments){
            jsonArray.put(commitment.toString());
        }
        return jsonArray.toString();
    }

    public byte[] getHash(){
        return SHA512.hash(toString());
    }

    public static CommitmentList fromString(String from){
        JSONArray jsonArray = new JSONArray(from);
        CommitmentList commitmentList = new CommitmentList();
        for (int i = 0; i < jsonArray.length() ; i++){
            String record = jsonArray.getString(i);
            commitmentList.add(
                    ExecutorCommitment.fromString(
                            record
                    )
            );
        }
        return commitmentList;
    }

}
