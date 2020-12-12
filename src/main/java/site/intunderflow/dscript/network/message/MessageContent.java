package site.intunderflow.dscript.network.message;

public interface MessageContent {

    String toString();

    Message toMessage(int withReach);

    String getType();

}
