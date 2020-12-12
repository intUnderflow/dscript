package site.intunderflow.dscript.application.blocklattice.blockchain.block;

import com.google.common.base.Preconditions;
import org.json.JSONObject;
import site.intunderflow.dscript.application.blocklattice.blockchain.BlockBytesFromStringFactory;
import site.intunderflow.dscript.application.blocklattice.blockchain.address.BaseAddress;
import site.intunderflow.dscript.utility.Time;

public class NameRegistration extends Block {

    private final String name;

    private final BaseAddress baseAddress;

    private final long utcTimestamp;

    public NameRegistration(String name, BaseAddress baseAddress){
        this(name, baseAddress, Time.getUTCTimestampInSeconds());
    }

    private NameRegistration(String name, BaseAddress baseAddress, long timestamp){
        this.name = Preconditions.checkNotNull(name);
        Preconditions.checkArgument(nameAcceptable(name), "Name is not acceptable.");
        this.baseAddress = Preconditions.checkNotNull(baseAddress);
        this.utcTimestamp = timestamp;
    }

    private boolean nameAcceptable(String nameToCheck){
        return nameToCheck.matches("[A-Za-z0-9]+") && nameToCheck.equals(name.toLowerCase())
                && nameToCheck.length() <= 20 && nameToCheck.length() > 0;
    }

    @Override
    public String toString(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", getType());
        jsonObject.put("name", name);
        jsonObject.put("address", baseAddress.toString());
        jsonObject.put("timestamp", utcTimestamp);
        return jsonObject.toString();
    }

    @Override
    public long getUTCTimestampInSeconds(){
        return utcTimestamp;
    }

    @Override
    public byte[] toBytes(){
        return new BlockBytesFromStringFactory(toString()).getBytes();
    }

    @Override
    public byte[] getPreviousReference(){
        return new byte[0];
    }

    @Override
    public boolean isGenesis(){
        return true;
    }

    @Override
    public String getType(){
        return "name_registration";
    }

    public static NameRegistration fromString(String from){
        JSONObject jsonObject = new JSONObject(from);
        return new NameRegistration(
                jsonObject.getString("name"),
                new BaseAddress(jsonObject.getString("address")),
                jsonObject.getLong("timestamp")
        );
    }

    public String getName() {
        return name;
    }

    public BaseAddress getAddress() {
        return baseAddress;
    }

    @Override
    public String getAccountType(){
        return null;
    }

}
