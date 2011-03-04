package fr.xebia.usiquizz.server.http.deft;

import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.deftserver.web.http.HttpRequest;
import org.deftserver.web.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.xebia.usiquizz.core.game.Game;
import fr.xebia.usiquizz.core.persistence.UserRepository;

public class LoginResourceRequestHandler extends RestHandler {

    private static Logger logger = LoggerFactory
            .getLogger(LoginResourceRequestHandler.class);

    private UserRepository userRepository;

    private Game game;

    private final AsyncResponseQueue queue;

<<<<<<< HEAD
    // private ExecutorService threadExecutor = Executors.newFixedThreadPool(3);
=======
//    private ExecutorService threadExecutor = Executors.newFixedThreadPool(3);
>>>>>>> bb18d3d2af7fe9843a23853c806c4104c89292ef

    public LoginResourceRequestHandler(UserRepository userRepository, Game game) {
        this.userRepository = userRepository;
        this.game = game;
        queue = new AsyncResponseQueue();
    }

    @Override
    public void post(final HttpRequest request, final HttpResponse response) {
<<<<<<< HEAD
        logger.debug("REST call for path {}", request.getRequestedPath());
        // logger.trace("Message : " + request.getBody());
        /*
         * if (game.isGameStarted()) { logger.debug("Game not started :p");
         * response.setStatusCode(400); response.write("Bad request !"); return;
         * }
         */
=======
        logger.debug("REST call for path " + request.getRequestedPath());
        logger.trace("Message : " + request.getBody());
        // FIXME
        //if (game.isGameStarted()) {
        //    response.setStatusCode(400);
        //    return;
        //}
>>>>>>> bb18d3d2af7fe9843a23853c806c4104c89292ef
        try {
            String mail = null;
            String password = null;
            JsonParser jp = jsonFactory.createJsonParser(request
                    .getBodyBuffer().array(), 0, request.getBodyBuffer()
                    .limit());
            jp.nextToken(); // will return JsonToken.START_OBJECT (verify?)
            while (jp.nextToken() != JsonToken.END_OBJECT) {
                String fieldname = jp.getCurrentName();
                jp.nextToken(); // move to value, or START_OBJECT/START_ARRAY
                if ("mail".equals(fieldname)) { // contains an object
                    mail = jp.getText();
                } else if ("password".equals(fieldname)) {
                    password = jp.getText();
<<<<<<< HEAD
                }
                // else {
                // throw new IllegalStateException("Unrecognized field '" +
                // fieldname + "'!");
                // }
            }
            if (mail != null && password != null) {

                boolean res = userRepository.logUser(mail, password);
                if (res) {

                    initCookie(request, response);
                    response.setStatusCode(200);
                    response.write("logged");
                    logger.debug("User {} successfully logged in", res);
=======
                } else {
                    throw new IllegalStateException("Unrecognized field '" + fieldname + "'!");
                }
            }
            if (mail != null && password != null) {
                if (userRepository.logUser(mail, password)) {
                    initCookie(request, response);
                    response.setStatusCode(200);
                    response.write("logged");

>>>>>>> bb18d3d2af7fe9843a23853c806c4104c89292ef
                } else {
                    response.setStatusCode(400);
                    response.write("Failed");
                    logger.info("logging failed found user is null");

                }
<<<<<<< HEAD
                return;

                // Asyn call
                // queue.planify();
                /*
                 * asynclogUser(mail, password, new AsyncResult<User>() {
                 * 
                 * @Override public void onFailure(Throwable throwable) {
                 * response.setStatusCode(400);
                 * queue.pushResponseToSend(response); }
                 * 
                 * @Override public void onSuccess(User user) { if (user !=
                 * null) { initCookie(request, response);
                 * response.setStatusCode(200); response.write("logged");
                 * 
                 * } else { response.setStatusCode(400);
                 * logger.info("logging failed");
                 * 
                 * } queue.pushResponseToSend(response); } });
                 */

            }
        } catch (IOException e1) {
            logger.error("IO error on logging", e1);
        }
        // ERROR
        response.setStatusCode(400);
        response.write("Bad request !");
=======

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
        } catch (IOException e1) {
            logger.info("IO error on logging", e1);
        }
        // ERROR
        response.setStatusCode(400);

>>>>>>> bb18d3d2af7fe9843a23853c806c4104c89292ef

    }

    // private void asynclogUser(final String mail, final String password, final
    // AsyncResult<User> callback) {
    // threadExecutor.execute(new Runnable() {
    // @Override
    // public void run() {
    // try {
    // callback.onSuccess(userRepository.logUser(mail, password));
    // }
    // catch (Exception e) {
    // callback.onFailure(e);
    // }
    // }
    // });
    // }
}
