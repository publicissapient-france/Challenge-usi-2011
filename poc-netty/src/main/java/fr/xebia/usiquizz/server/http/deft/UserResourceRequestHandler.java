package fr.xebia.usiquizz.server.http.deft;

import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.deftserver.web.http.HttpRequest;
import org.deftserver.web.http.HttpResponse;


import fr.xebia.usiquizz.core.persistence.UserRepository;

public class UserResourceRequestHandler extends RestHandler {
	
	final UserRepository repo;
	
	public UserResourceRequestHandler(UserRepository repo) {
		this.repo = repo;
	}
	
	@Override
	public void post(HttpRequest request, HttpResponse response) {


        //String content = request.getBody();
        String email = null;
        String password = null;
        String firstname = null;
        String lastname = null;

        JsonParser jp = null;
        try {
            jp = jsonFactory.createJsonParser(request.getBodyBuffer().array(), 0, request.getBodyBuffer().limit());
            jp.nextToken();
            while (jp.nextToken() != JsonToken.END_OBJECT) {
                String fieldname = jp.getCurrentName();
                jp.nextToken();
                if ("mail".equals(fieldname)) {
                    email = jp.getText();
                }
                else if ("password".equals(fieldname)) {
                    password = jp.getText();
                }
                else if ("firstname".equals(fieldname)) {
                    firstname = jp.getText();
                }
                else if ("lastname".equals(fieldname)) {
                    lastname = jp.getText();
                }
                else {
                    throw new IllegalStateException("Unrecognized field '" + fieldname + "'!");
                }
            }

            repo.insertUser(email, password, firstname, lastname);
            response.setStatusCode(200);
            response.write("Success");
            return;
        }
        catch (IOException e1) {
            e1.printStackTrace();
        }
        response.setStatusCode(400);
	}
}
