package site.intunderflow.dscript.network.message.content;

import com.google.common.base.Preconditions;
import org.json.JSONObject;
import site.intunderflow.dscript.application.blocklattice.blockchain.address.BaseAddress;
import site.intunderflow.dscript.network.message.Message;
import site.intunderflow.dscript.network.message.MessageContent;
import site.intunderflow.dscript.network.message.MessageWithReachFactory;
import site.intunderflow.dscript.utility.Hex;

public class ExecutionRequest implements MessageContent {

    private final BaseAddress dappAddress;

    private final byte[] input;

    public ExecutionRequest(
            BaseAddress dappAddress,
            byte[] input
    ){
        this.dappAddress = Preconditions.checkNotNull(dappAddress);
        this.input = Preconditions.checkNotNull(input);
    }

    @Override
    public String getType(){
        return this.getClass().getSimpleName();
    }

    @Override
    public String toString(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", getType());
        jsonObject.put("input", Hex.encode(input));
        jsonObject.put("dapp", dappAddress.toString());
        return jsonObject.toString();
    }

    @Override
    public Message toMessage(int reach){
        return new MessageWithReachFactory(this).create(reach);
    }

    public static ExecutionRequest fromString(String from){
        JSONObject jsonObject = new JSONObject(from);
        return new ExecutionRequest(
                BaseAddress.fromString(
                        jsonObject.getString("dapp")
                ),
                Hex.decode(
                        jsonObject.getString("input")
                )
        );
    }

    public BaseAddress getDappAddress() {
        return dappAddress;
    }

    public byte[] getInput() {
        return input;
    }

}
