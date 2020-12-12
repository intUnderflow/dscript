package site.intunderflow.dscript.network.cluster.rest;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.utility.DoubleConsumer;
import spark.Request;
import spark.Response;
import spark.Route;

class ConsumerSparkRoute implements Route {

    private final DoubleConsumer<Request, Response> consumer;

    public ConsumerSparkRoute(DoubleConsumer<Request, Response> consumer){
        this.consumer = Preconditions.checkNotNull(consumer);
    }

    @Override
    public Object handle(Request request, Response response){
        consumer.accept(request, response);
        response.header("Access-Control-Allow-Origin", "*");
        response.header("Access-Control-Allow-Methods","GET,POST,PUT,DELETE");
        response.header("Access-Control-Allow-Headers","Content-Type");
        return response.body();
    }

}