package fr.xebia.usiquizz.injector;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Response;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class HttpClientTest {

    private static final int NB_REQ = 1000;

    public static void main(String[] args) throws InterruptedException, IOException {
        AsyncHttpClientConfig.Builder configBuilder = new AsyncHttpClientConfig.Builder();
        configBuilder.setMaximumConnectionsPerHost(15000);
        configBuilder.setMaximumConnectionsTotal(100000);
        AsyncHttpClient client = new AsyncHttpClient(configBuilder.build());
        final CountDownLatch cdl = new CountDownLatch(NB_REQ);


        // Setup the request 2

        long start = System.nanoTime();
        // Execute the request 3
        for (int i = 0; i < NB_REQ; i++) {
            client.prepareGet("http://localhost:8080/static/html/index.html").execute(new AsyncCompletionHandler<Void>() {
                @Override
                public Void onCompleted(Response response) throws Exception {
                    cdl.countDown();
                    return null;
                }
            });

            cdl.await();
            double takeTime = System.nanoTime() - start;
            System.out.println("Time : " + takeTime / 1000000 + " ms");
            System.out.println("Throughput : " + (double) NB_REQ / (takeTime / 1000000000) + " inv/s");

        }

    }
}