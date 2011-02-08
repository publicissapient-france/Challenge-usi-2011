package fr.xebia.usiquizz.test.ning;

import com.ning.http.client.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

public class HttpTestLogin {

    private static final int NB_CLIENT = 1000;

    private static final AtomicLong nbRequestSend = new AtomicLong(0);
    private static final AtomicLong nbLogin = new AtomicLong(0);
    private static final AtomicLong nbRequestCompleted = new AtomicLong(0);

    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        AsyncHttpClientConfig.Builder configBuilder = new AsyncHttpClientConfig.Builder();
        configBuilder.setMaximumConnectionsPerHost(15000);
        configBuilder.setMaximumConnectionsTotal(100000);
        AsyncHttpClient c = new AsyncHttpClient(configBuilder.build());
        File loginFile = new File("src/test/test-file/logins.json");
        List<Future<String>> futures = new ArrayList<Future<String>>();

        BufferedReader loginReader = new BufferedReader(new FileReader(loginFile));
        final long start = System.nanoTime();
        for (int i = 0; i < NB_CLIENT; i++) {
            futures.add(sendRequest(c, "http://localhost:8080/api/login", loginReader.readLine()));
            nbRequestSend.incrementAndGet();
        }
        final long loggedTime = System.nanoTime();
        System.out.println("Take : " + ((double) (loggedTime - start)) / 1000000d + " ms");

        for (Future<String> future : futures) {
            future.get();
            nbRequestCompleted.incrementAndGet();
        }
        final long end = System.nanoTime();
        System.out.println("Take : " + ((double) (end - start)) / 1000000d + " ms for complete");
        c.close();
    }

    private static Future<String> sendRequest(AsyncHttpClient c, String requestUrl, String body) throws IOException {
        AsyncHttpClient.BoundRequestBuilder request = c.preparePost(requestUrl);
        request.setBody(body);
        return request.execute(new AsyncHandler<String>() {
            private StringBuilder builder = new StringBuilder();

            @Override
            public STATE onStatusReceived(HttpResponseStatus status) throws Exception {
                int statusCode = status.getStatusCode();
                //System.out.println("Received status : " + statusCode);
                // The Status have been read
                // If you don't want to read the headers,body or stop processing the response
                return STATE.CONTINUE;
            }

            @Override
            public STATE onHeadersReceived(HttpResponseHeaders h) throws Exception {
                FluentCaseInsensitiveStringsMap headers = h.getHeaders();
                //System.out.println("");
                //System.out.println("Headers received...");
                if (headers != null) {
                    for (String name : headers.keySet()) {
                        //System.out.println(name + " : " + headers.getHeaderValue(name));
                    }
                }
                //System.out.println("");
                // The headers have been read
                // If you don't want to read the body, or stop processing the response
                return STATE.CONTINUE;
            }

            @Override
            public STATE onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
                builder.append(new String(bodyPart.getBodyPartBytes()));
                nbLogin.incrementAndGet();
                return STATE.CONTINUE;
            }

            @Override
            public String onCompleted() throws Exception {
                // Will be invoked once the response has been fully read or a ResponseComplete exception
                // has been thrown.
                return builder.toString();
            }

            @Override
            public void onThrowable(Throwable t) {
            }
        });
    }

}
