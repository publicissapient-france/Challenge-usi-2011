package fr.xebia.usiquizz.server.http.deft;


import org.deftserver.io.IOLoop;
import org.deftserver.web.Application;
import org.deftserver.web.Asynchronous;
import org.deftserver.web.HttpServer;
import org.deftserver.web.handler.RequestHandler;
import org.deftserver.web.http.HttpRequest;
import org.deftserver.web.http.HttpResponse;

import java.util.HashMap;
import java.util.Map;

public class DeftHttpServer {


    public static void main(String[] args) {
        Map<String, RequestHandler> handlers = new HashMap<String, RequestHandler>();
        handlers.put("/", new AsynchronousRequestHandler());
        HttpServer server = new HttpServer(new Application(handlers));
        server.listen(8080);
        IOLoop.INSTANCE.start();
    }
}

class AsynchronousRequestHandler extends RequestHandler {

    @Override
    @Asynchronous
    public void get(HttpRequest request, final HttpResponse response) {
        response.write("hello ");
        db.asyncIdentityGet("world", new AsyncCallback<String>() {
            public void onSuccess(String result) { response.write(result).finish(); }
        });
    }
}
