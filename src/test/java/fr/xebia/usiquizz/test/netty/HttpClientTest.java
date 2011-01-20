package fr.xebia.usiquizz.test.netty;


import org.factor45.hotpotato.client.DefaultHttpClient;
import org.factor45.hotpotato.request.HttpRequestFuture;
import org.factor45.hotpotato.request.HttpRequestFutureListener;
import org.factor45.hotpotato.response.BodyAsStringProcessor;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;

import java.util.concurrent.CountDownLatch;

public class HttpClientTest {

    private static final int NB_REQ = 1000;

    public static void main(String[] args) throws InterruptedException {
        DefaultHttpClient client = new DefaultHttpClient();
        client.setMaxQueuedRequests(200000);
        client.setMaxConnectionsPerHost(2000);
        client.setRequestTimeoutInMillis(1200000);
        client.setUseNio(true);
        final CountDownLatch cdl = new CountDownLatch(NB_REQ);
        if (!client.init()) {
            return;
        }

        // Setup the request 2
        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1,
                HttpMethod.GET, "/static/html/index.html");
        long start = System.nanoTime();
        // Execute the request 3
        for (int i = 0; i < NB_REQ; i++) {
            HttpRequestFuture future = client.execute("localhost", 8080, request, new BodyAsStringProcessor());
            future.addListener(new HttpRequestFutureListener<String>() {
                @Override
                public void operationComplete(HttpRequestFuture future) throws Exception {
                    cdl.countDown();
                    //System.out.println(future);
                    if (future.isSuccessfulResponse()) {
                        //  System.out.println(future.getProcessedResult());
                    }
                }
            });
        }

        cdl.await();
        double takeTime = System.nanoTime() - start;
        System.out.println("Time : " + takeTime/1000000 +" ms");
        System.out.println("Throughput : " + (double) NB_REQ / (takeTime / 1000000000) + " inv/s");

        // Cleanup 4
        client.terminate();
    }

}
