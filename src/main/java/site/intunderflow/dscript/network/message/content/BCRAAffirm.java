package site.intunderflow.dscript.network.message.content;

import com.google.common.base.Preconditions;
import org.json.JSONObject;
import site.intunderflow.dscript.application.bcra.commitment.AffirmationSignature;
import site.intunderflow.dscript.network.message.Message;
import site.intunderflow.dscript.network.message.MessageContent;
import site.intunderflow.dscript.network.message.MessageWithReachFactory;

public class BCRAAffirm implements MessageContent {

    private final AffirmationSignature affirmationSignature;

    public BCRAAffirm(
            AffirmationSignature affirmationSignature
    ){
        this.affirmationSignature = Preconditions.checkNotNull(affirmationSignature);
    }

    @Override
    public String getType(){
        return this.getClass().getSimpleName();
    }

    @Override
    public Message toMessage(int reach){
        return new MessageWithReachFactory(this).create(reach);
    }

    @Override
    public String toString(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", getType());
        jsonObject.put("signature", affirmationSignature.toString());
        return jsonObject.toString();
    }

    public AffirmationSignature getAffirmationSignature(){
        return affirmationSignature;
    }

    public static BCRAAffirm fromString(String from){
        JSONObject jsonObject = new JSONObject(from);
        return new BCRAAffirm(
                AffirmationSignature.fromString(jsonObject.getString("signature"))
        );
    }

}
