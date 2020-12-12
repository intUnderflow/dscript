package site.intunderflow.dscript.application.bcra.commitment;

import com.google.common.base.Preconditions;
import org.json.JSONObject;
import site.intunderflow.dscript.application.consensus.dpos.Signature;
import site.intunderflow.dscript.application.consensus.dpos.SignatureFactory;

import java.security.GeneralSecurityException;

public class SignedCommitmentList {

    private final CommitmentList commitmentList;

    private final Signature signature;

    public SignedCommitmentList(CommitmentList commitmentList, Signature signature){
        this.commitmentList = Preconditions.checkNotNull(commitmentList);
        this.signature = Preconditions.checkNotNull(signature);
    }

    public SignedCommitmentList(CommitmentList commitmentList, SignatureFactory signatureFactory)
        throws GeneralSecurityException {
        this(commitmentList, signatureFactory.sign(
                commitmentList.getHash()
        ));
    }

    public CommitmentList getList(){
        return commitmentList;
    }

    public Signature getSignature() {
        return signature;
    }

    @Override
    public String toString(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("list", commitmentList.toString());
        jsonObject.put("signature", signature.toString());
        return jsonObject.toString();
    }

    public static SignedCommitmentList fromString(String from){
        JSONObject jsonObject = new JSONObject(from);
        CommitmentList commitmentList = CommitmentList.fromString(jsonObject.getString("list"));
        Signature signature = Signature.fromString(jsonObject.getString("signature"));
        return new SignedCommitmentList(
                commitmentList,
                signature
        );
    }

}
