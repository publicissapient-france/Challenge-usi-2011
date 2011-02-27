package fr.xebia.usiquizz.server.http.netty;

import fr.xebia.usiquizz.server.http.netty.resources.CachedResourcesRequestHandler;
import fr.xebia.usiquizz.server.http.netty.rest.RestRequestHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpRequest;

public class HttpRequestHandler extends SimpleChannelUpstreamHandler {

    private static final String API_PATH = "/api/";
    private static final String STATIC_PATH = "/static/";
    private static final String INDEX_PATH = "/";

    private RestRequestHandler restRequestHandler;

    private CachedResourcesRequestHandler staticRequestHandler;

    public HttpRequestHandler(RestRequestHandler restRequestHandler, CachedResourcesRequestHandler staticRequestHandler) {
        this.restRequestHandler = restRequestHandler;
        this.staticRequestHandler = staticRequestHandler;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

        HttpRequest request = (HttpRequest) e.getMessage();
        /**
         *  if pattern /api then use RestHandler
         *  if pattern /static then use static handler
         *  if pattern / redirect to /static/html/index.html
         */
        String uri = request.getUri();
        if (uri != null) {
            if (uri.startsWith(API_PATH)) {
                restRequestHandler.messageReceived(uri.substring(5), ctx, e);
            }
            else if (uri.startsWith(STATIC_PATH)) {
                staticRequestHandler.messageReceived(ctx, e);
            }
            else if (uri.equals(INDEX_PATH)) {

            }
        }
    }

}
