package site.intunderflow.dscript.network.message.content;

import com.google.common.base.Preconditions;
import org.json.JSONObject;
import site.intunderflow.dscript.network.cluster.address.Address;
import site.intunderflow.dscript.network.message.Message;
import site.intunderflow.dscript.network.message.MessageContent;
import site.intunderflow.dscript.network.message.MessageWithReachFactory;

public class AddressAnnouncement implements MessageContent {

    private final String content;

    private final Address address;

    public AddressAnnouncement(Address address) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", getType());
        jsonObject.put("address", address.toString());
        this.content = Preconditions.checkNotNull(jsonObject.toString());
        this.address = Preconditions.checkNotNull(address);
    }

    private AddressAnnouncement(
            Address address,
            String content
    ) {
        this.content = content;
        this.address = address;
    }

    public static AddressAnnouncement fromString(String content) {
        JSONObject jsonObject = new JSONObject(content);
        return new AddressAnnouncement(
                Address.fromString(jsonObject.getString("address")),
                content
        );
    }

    @Override
    public String toString() {
        return content;
    }

    @Override
    public Message toMessage(int reach) {
        return new MessageWithReachFactory(this).create(reach);
    }

    @Override
    public String getType() {
        return this.getClass().getSimpleName();
    }

    public Address getAddress() {
        return address;
    }
}
