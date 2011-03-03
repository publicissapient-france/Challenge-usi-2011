package fr.xebia.usiquizz.server.http.deft;

import fr.xebia.usiquizz.core.game.Game;
import fr.xebia.usiquizz.core.persistence.User;
import fr.xebia.usiquizz.core.persistence.UserRepository;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.deftserver.web.AsyncResult;
import org.deftserver.web.Asynchronous;
import org.deftserver.web.http.HttpRequest;
import org.deftserver.web.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginResourceRequestHandler extends RestHandler {

    private static Logger logger = LoggerFactory.getLogger(LoginResourceRequestHandler.class);

    private UserRepository userRepository;

    private Game game;

    private final AsyncResponseQueue queue;
    
//    private ExecutorService threadExecutor = Executors.newFixedThreadPool(3);

    public LoginResourceRequestHandler(UserRepository userRepository, Game game) {
        this.userRepository = userRepository;
        this.game = game;
        queue = new AsyncResponseQueue();
    }

    @Override
    public void post(final HttpRequest request, final HttpResponse response) {
        logger.debug("REST call for path {}" , request.getRequestedPath());
   //     logger.trace("Message : " + request.getBody());
        if (game.isGameStarted()) {
        	logger.debug("Game not started :p");
            response.setStatusCode(400);
            response.write("Bad request !");
            return;
        }
        try {
            String mail = null;
            String password = null;
            JsonParser jp = jsonFactory.createJsonParser(request.getBodyBuffer().array(), 0, request.getBodyBuffer().limit());
            jp.nextToken(); // will return JsonToken.START_OBJECT (verify?)
            while (jp.nextToken() != JsonToken.END_OBJECT) {
                String fieldname = jp.getCurrentName();
                jp.nextToken(); // move to value, or START_OBJECT/START_ARRAY
                if ("mail".equals(fieldname)) { // contains an object
                    mail = jp.getText();
                }
                else if ("password".equals(fieldname)) {
                    password = jp.getText();
                }
                else {
                    break;
                }
            }
            if (mail != null && password != null) {
            	
            	User usr = userRepository.logUser(mail, password);
            	if (usr != null) {
            		
                    initCookie(request, response);
                    response.setStatusCode(200);
                    response.write("logged");
                    logger.debug("User {} successfully logged in", usr);
                }
                else {
                    response.setStatusCode(400);
                    response.write("Failed");
                    logger.info("logging failed found user is null");
                  
                }
            	return;
     
                // Asyn call
            	//queue.planify();
    /*            asynclogUser(mail, password, new AsyncResult<User>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        response.setStatusCode(400);
                        queue.pushResponseToSend(response);
                    }

                    @Override
                    public void onSuccess(User user) {
                        if (user != null) {
                            initCookie(request, response);
                            response.setStatusCode(200);
                            response.write("logged");
                            
                        }
                        else {
                            response.setStatusCode(400);
                            logger.info("logging failed");
                          
                        }
                        queue.pushResponseToSend(response);
                    }
                });*/
                
   
            }
        }
        catch (IOException e1) {
        	logger.error("IO error on logging", e1);
        }
        // ERROR
        response.setStatusCode(400);
        response.write("Bad request !");
 
    }

//    private void asynclogUser(final String mail, final String password, final AsyncResult<User> callback) {
//        threadExecutor.execute(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    callback.onSuccess(userRepository.logUser(mail, password));
//                }
//                catch (Exception e) {
//                    callback.onFailure(e);
//                }
//            }
//        });
//    }
}
