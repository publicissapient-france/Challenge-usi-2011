package fr.xebia.usiquizz.test.ning;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Response;
import fr.xebia.usiquizz.test.mongodb.ReinitDatabase;

import java.io.*;

public class HttpCreateUser {

    private static AsyncHttpClient client;
    private static long i = 0;
    private static long userCreated = 0;
    private static String host;
    private static final String DEFAULT_HOST = "127.0.0.1:8080";

    public static void main(String[] args) throws IOException, FileNotFoundException {
        ReinitDatabase.main(args);
        if (args.length > 0) {
            host = args[0];
        }
        else {
            host = DEFAULT_HOST;
        }

        AsyncHttpClientConfig.Builder configBuilder = new AsyncHttpClientConfig.Builder();
        configBuilder.setMaximumConnectionsPerHost(100000);
        configBuilder.setMaximumConnectionsTotal(1000000);
        client = new AsyncHttpClient(configBuilder.build());

        File userFile = new File("src/test/test-file/users.json");

        BufferedReader userReader = new BufferedReader(new FileReader(userFile));
        long start = System.nanoTime();
        while (userReader.ready()) {
            postCreateUser(userReader.readLine(), host);
            i++;
        }
        double takeTime = System.nanoTime() - start;
        System.out.println("Time : " + takeTime / 1000000 + " ms");
        System.out.println("Throughput : " + (double) i / (takeTime / 1000000000) + " inv/s");

        while (userCreated + 500 < i) {
            System.out.println(userCreated + " : user created on " + i + " users");
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        System.out.println(userCreated + " : user created on " + i + " users");
        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        // Cleanup 4
        client.close();
    }

    private static void postCreateUser(String jsonCreateUser, String host) throws IOException {

        AsyncHttpClient.BoundRequestBuilder request = client.preparePost("http://" + host + "/api/user");
        request.setBody(jsonCreateUser);
        request.execute(new AsyncCompletionHandler<Void>() {
            @Override
            public Void onCompleted(Response response) throws Exception {
                userCreated++;
                return null;
            }
        });
    }
}
