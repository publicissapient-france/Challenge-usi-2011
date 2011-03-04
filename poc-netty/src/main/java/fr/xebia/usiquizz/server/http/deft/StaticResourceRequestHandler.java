package fr.xebia.usiquizz.server.http.deft;

import org.deftserver.web.http.HttpRequest;
import org.deftserver.web.http.HttpResponse;

public class StaticResourceRequestHandler extends RestHandler {
	
	
	@Override
	public void get(HttpRequest request, HttpResponse response) {
		response.write("OK");
		response.finish();
	}
}
