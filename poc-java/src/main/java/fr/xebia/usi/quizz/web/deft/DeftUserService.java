package fr.xebia.usi.quizz.web.deft;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.deftserver.io.IOLoop;
import org.deftserver.io.timeout.Timeout;
import org.deftserver.web.Application;
import org.deftserver.web.AsyncCallback;
import org.deftserver.web.AsyncResult;
import org.deftserver.web.Asynchronous;
import org.deftserver.web.HttpServer;
import org.deftserver.web.handler.RequestHandler;
import org.deftserver.web.http.HttpRequest;
import org.deftserver.web.http.HttpResponse;
import org.deftserver.web.http.HttpServerDescriptor;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import fr.xebia.usi.quizz.model.User;
import fr.xebia.usi.quizz.service.JsonMapper;
import fr.xebia.usi.quizz.service.JsonMapperImpl;
import fr.xebia.usi.quizz.service.UserManager;
import fr.xebia.usi.quizz.service.UserManagerMongoImpl;


/**
 * Try it on Deft
 * @author slm
 *
 */
public class DeftUserService extends RequestHandler {

	private final UserManager manager;
	private final JsonMapper mapper;
	private final Executor executor;
	
	public DeftUserService() {
		this.manager = new UserManagerMongoImpl();
		this.mapper = new JsonMapperImpl();
		this.executor = Executors.newFixedThreadPool(10);
	}
	
	
        @Override
        public void get(HttpRequest request, HttpResponse response) {
        	 response.setStatusCode(400);
            response.write("No GET Service for now !");
        }

        @Override
        @Asynchronous
        public void post(final HttpRequest request, final HttpResponse response) {
        	
  
        	final User usr = mapper.mapJsonUser(request.getBody().getBytes());
        	
        	if (usr == null){
        		response.setStatusCode(400);
        		response.write("Bad user Json format !").finish();
        		return;
        	}
        	
        	final UserCallBack cb = new UserCallBack(request, response);
        	
        	// Save the user and send response
        	executor.execute(new Runnable() {
				
				@Override
				public void run() {
					// If user does not exist
					if (manager.getUser(usr.getMail()) == null) {
						manager.save(usr);
						cb.onSuccess(usr);
					}
					else {
						cb.onFailure(null);
					}
				}
			});

        }
        
        
        private class UserCallBack implements AsyncResult<User> {
        	final HttpRequest request;
        	final HttpResponse response;
        	
        	public UserCallBack(HttpRequest request, HttpResponse response) {
				this.request = request;
				this.response = response;
			}
        	
        	
        	@Override
        	public void onFailure(Throwable arg0) {
        	

        		response.setStatusCode(400);
        		response.write("Bad user");
        		//response.flush();
        		response.finish();
        	}
        	
        	@Override
        	public void onSuccess(User arg0) {
        		
        		response.setStatusCode(201);
        		response.write("OK User saved :)");
   
        		response.finish();
        	}
        }

    public static void main(String[] args) {
        Map<String, RequestHandler> handlers = new HashMap<String, RequestHandler>();
        handlers.put("/", new RequestHandler() {
        	
        	@Override
        	public void get(HttpRequest request, HttpResponse response) {
        		// TODO Auto-generated method stub
        		super.get(request, response);
        	}
        	
		});
        handlers.put("/api/user", new DeftUserService());
        HttpServerDescriptor.KEEP_ALIVE_TIMEOUT = 30 * 1000;	// 30s  
		HttpServerDescriptor.READ_BUFFER_SIZE = 1500;			// 1500 bytes 
		HttpServerDescriptor.WRITE_BUFFER_SIZE = 1500;			// 1500 bytes 

        try {
			HttpServer server = new org.deftserver.web.HttpServer(new Application(handlers));
			server.listen(8080);
			IOLoop.INSTANCE.start();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
    }
	
	
}
