package site.intunderflow.dscript.webpanel.pages;

import spark.Request;
import spark.Response;

public interface Page {

    String getPathToMatch();

    void accept(Request request, Response response);

    int getPriority();

}
