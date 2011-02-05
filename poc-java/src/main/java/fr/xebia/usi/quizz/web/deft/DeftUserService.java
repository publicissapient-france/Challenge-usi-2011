package fr.xebia.usi.quizz.web.deft;

import java.util.HashMap;
import java.util.Map;

import org.deftserver.io.IOLoop;
import org.deftserver.io.timeout.Timeout;
import org.deftserver.web.Application;
import org.deftserver.web.AsyncCallback;
import org.deftserver.web.Asynchronous;
import org.deftserver.web.HttpServer;
import org.deftserver.web.handler.RequestHandler;
import org.deftserver.web.http.HttpRequest;
import org.deftserver.web.http.HttpResponse;



public class DeftUserService extends RequestHandler {

        @Override
        public void get(HttpRequest request, HttpResponse response) {
            response.write("hello world!");
        }

        @Override
        @Asynchronous
        public void post(HttpRequest request, HttpResponse response) {
        	
        	IOLoop.INSTANCE.addTimeout(new Timeout(100, new AsyncCallback() {
				
				@Override
				public void onCallback() {
					
					
				}
			}));
        	request.getBody();

        }

    public static void main(String[] args) {
        Map<String, RequestHandler> handlers = new HashMap<String, RequestHandler>();
        handlers.put("/api/user", new DeftUserService());
        HttpServer server = new org.deftserver.web.HttpServer(new Application(handlers));
        server.listen(8080);
        IOLoop.INSTANCE.start();
    }
	
	
}
