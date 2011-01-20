package fr.xebia.usiquizz.test.netty;


import org.factor45.hotpotato.client.DefaultHttpClient;
import org.factor45.hotpotato.request.HttpRequestFuture;
import org.factor45.hotpotato.response.HttpResponseProcessor;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.*;
import org.jboss.netty.util.CharsetUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class HttpTestLogin {

    private static long nbCallValidated = 0;
    private static long nbCallSuccess = 0;

    public static void main(String[] args) throws InterruptedException, IOException {
        File loginFile = new File("quizz-usi-core/src/test/test-file/logins.json");

        BufferedReader loginReader = new BufferedReader(new FileReader(loginFile));
        DefaultHttpClient client = new DefaultHttpClient();
        client.setMaxQueuedRequests(200000);
        client.setMaxConnectionsPerHost(100000);
        client.setRequestTimeoutInMillis(10000000);
        client.setUseNio(true);
        long nbCall = 0;
        if (!client.init()) {
            return;
        }

        // Setup the request 2
        long start = System.nanoTime();
        // Execute the request 3
        while (loginReader.ready()) {
            HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1,
                    HttpMethod.POST, "/api/login");
            ChannelBuffer cb = ChannelBuffers.copiedBuffer(loginReader.readLine(), CharsetUtil.UTF_8);
            request.setContent(cb);
            request.setHeader(HttpHeaders.Names.CONTENT_LENGTH, cb.readableBytes());
            HttpRequestFuture future = client.execute("localhost", 8080, request, new CountHttpResponseProcessor());
            nbCall++;

        }

        while (nbCallValidated + 500 < nbCall) {
            System.out.println(nbCallValidated + " : user logged");
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        double takeTime = System.nanoTime() - start;
        System.out.println("Time : " + takeTime / 1000000 + " ms");
        System.out.println(nbCallValidated + " call");
        System.out.println(nbCallSuccess + " user logged");
        System.out.println("Throughput : " + (double) nbCall / (takeTime / 1000000000) + " inv/s");

        Thread.sleep(1000);
        loginReader.close();

        // Cleanup 4
        client.terminate();
    }


    static class CountHttpResponseProcessor implements HttpResponseProcessor {
        private HttpResponse httpResponse;

        @Override
        public boolean willProcessResponse(HttpResponse httpResponse) throws Exception {
            this.httpResponse = httpResponse;
            return true;
        }

        @Override
        public void addData(ChannelBuffer channelBuffer) throws Exception {

        }

        @Override
        public void addLastData(ChannelBuffer channelBuffer) throws Exception {

        }

        @Override
        public Object getProcessedResponse() {
            if (httpResponse.getStatus().equals(HttpResponseStatus.OK)) {
                nbCallSuccess++;
            }
            nbCallValidated++;
            return null;
        }
    }
}
