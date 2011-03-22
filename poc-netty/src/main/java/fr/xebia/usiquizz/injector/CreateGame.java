package fr.xebia.usiquizz.injector;


import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class CreateGame {

    private static JsonFactory jf = new JsonFactory();


    public static void main(String... args) throws IOException, ExecutionException, InterruptedException {
        String host = "127.0.0.1:8080";
        String gameFile = "src/test/test-file/game.xml";
        if (args.length > 0) {
            host = args[0];
        }
        if (args.length > 1) {
            gameFile = args[1];
        }

        prepareGame(gameFile, host);
    }

    private static void prepareGame(String gameFileName, String host) throws IOException, ExecutionException, InterruptedException {
        AsyncHttpClientConfig.Builder configBuilder = new AsyncHttpClientConfig.Builder();
        configBuilder.setMaximumConnectionsPerHost(100000);
        configBuilder.setMaximumConnectionsTotal(1000000);
        // Timeout 10 minutes
        configBuilder.setRequestTimeoutInMs(60000);
        configBuilder.setExecutorService(Executors.newCachedThreadPool());
        configBuilder.setAllowPoolingConnection(false);
        // Better perf, but less logging
        //AsyncHttpProviderConfig nettyConfig = new NettyAsyncHttpProviderConfig();
        //nettyConfig.addProperty(NettyAsyncHttpProviderConfig.EXECUTE_ASYNC_CONNECT, "yes");
        //configBuilder.setAsyncHttpClientProviderConfig(nettyConfig);
        AsyncHttpClient client = new AsyncHttpClient(configBuilder.build());
        AsyncHttpClient.BoundRequestBuilder request = client.preparePost("http://" + host + "/api/game");
        request.setBody(createJsonGame(gameFileName));
        request.execute().get();
    }

    private static byte[] createJsonGame(String gameFileName) throws IOException {
        StringWriter sw = new StringWriter();
        JsonGenerator jgen = jf.createJsonGenerator(sw);

        jgen.writeStartObject();
        jgen.writeStringField("authentication_key", "xebia");
        jgen.writeFieldName("parameters");
        jgen.writeString(loadContent(gameFileName));
        jgen.writeEndObject();
        jgen.close();

        return sw.getBuffer().toString().getBytes();
    }

    private static String loadContent(String gameFileName) throws IOException {
        int len;
        char[] chr = new char[4096];
        final StringBuffer buffer = new StringBuffer();
        final FileReader reader = new FileReader(gameFileName);
        try {
            while ((len = reader.read(chr)) > 0) {
                buffer.append(chr, 0, len);
            }
        } finally {
            reader.close();
        }
        return buffer.toString();
    }


}
