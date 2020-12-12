package site.intunderflow.dscript.network.cluster.rest;

import site.intunderflow.dscript.network.message.Message;
import spark.Request;
import spark.Response;

import java.util.function.Consumer;

public interface EndpointVersion {

    String getVersion();

    void handle(Request request, Response response);

    void setMessageHandler(Consumer<Message> handler);

}
