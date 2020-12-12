package site.intunderflow.dscript.network.cluster;

public class RequestException extends Exception {

    public RequestException(String message){
        super(message);
    }

    public RequestException(Exception exception){
        super(exception);
    }
}
