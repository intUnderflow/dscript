package site.intunderflow.dscript.network.message.content;

import com.google.common.base.Preconditions;
import org.json.JSONObject;
import site.intunderflow.dscript.application.bcra.commitment.SignedCommitmentList;
import site.intunderflow.dscript.network.message.Message;
import site.intunderflow.dscript.network.message.MessageContent;
import site.intunderflow.dscript.network.message.MessageWithReachFactory;

import java.util.List;

public class BCRAList implements MessageContent {

    private final SignedCommitmentList signedCommitmentList;

    public BCRAList(SignedCommitmentList signedCommitmentList){
        this.signedCommitmentList = Preconditions.checkNotNull(signedCommitmentList);
    }

    @Override
    public Message toMessage(int reach){
        return new MessageWithReachFactory(this).create(reach);
    }

    @Override
    public String getType(){
        return this.getClass().getSimpleName();
    }

    @Override
    public String toString(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", getType());
        jsonObject.put("list", signedCommitmentList.toString());
        return jsonObject.toString();
    }

    public SignedCommitmentList getSignedCommitmentList() {
        return signedCommitmentList;
    }

    public static BCRAList fromString(String from){
        JSONObject jsonObject = new JSONObject(from);
        return new BCRAList(
                SignedCommitmentList.fromString(
                        jsonObject.getString("list")
                )
        );
    }

}
