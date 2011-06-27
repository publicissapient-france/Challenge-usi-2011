package fr.xebia.usiquizz.injector;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Response;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;

import java.io.*;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicLong;

public class HttpCreateUser {

    private static AsyncHttpClient client;
    private static long i = 0;
    private static AtomicLong userCreated = new AtomicLong(0);
    private static AtomicLong errRequest = new AtomicLong(0);
    private static String host;
    private static String userFileName="src/test/test-file/1million_users_1.csv";
    private static int nbUserToCreate = 100000;
    private static final String DEFAULT_HOST = "127.0.0.1:7000";

    public static void main(String[] args) throws IOException, FileNotFoundException {
        if (args.length > 0) {
            host = args[0];
        } else {
            host = DEFAULT_HOST;
        }

        if (args.length > 1) {
            userFileName = args[1];
        }
        
        if (args.length > 2) {
            try {
                nbUserToCreate = Integer.parseInt(args[2]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        AsyncHttpClientConfig.Builder configBuilder = new AsyncHttpClientConfig.Builder();
        configBuilder.setMaximumConnectionsPerHost(100000);
        configBuilder.setMaximumConnectionsTotal(1000000);
        client = new AsyncHttpClient(configBuilder.build());

        File userFile = new File(userFileName);

        BufferedReader userReader = new BufferedReader(new FileReader(userFile));
        long start = System.nanoTime();
        // pass 1st line
        userReader.readLine();
        while (userReader.ready() && i < nbUserToCreate) {
            postCreateUser(generateJson(userReader.readLine()), host);
            i++;
            if (i % 1000 == 0) {
                System.out.println(i + " : user creation request on " + nbUserToCreate + " users");
            }
        }
        double takeTime = System.nanoTime() - start;
        System.out.println("Time : " + takeTime / 1000000 + " ms");
        System.out.println("Throughput : " + (double) i / (takeTime / 1000000000) + " inv/s");

        while (userCreated.get() + errRequest.get() < i) {
            System.out.println(userCreated + " : user created on " + i + " users");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        System.out.println(userCreated + " : user created on " + i + " users");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        // Cleanup 4
        client.close();


    }

    private static String generateJson(String s) {
        StringTokenizer st = new StringTokenizer(s, ",", false);
        StringTemplate jsonUsers = new StringTemplate("{\"firstname\":\"$firstname$\",\"lastname\":\"$lastname$\",\"mail\":\"$mail$\",\"password\":\"$password$\"}", DefaultTemplateLexer.class);
        jsonUsers.setAttribute("firstname", st.nextToken());
        jsonUsers.setAttribute("lastname", st.nextToken());
        jsonUsers.setAttribute("mail", st.nextToken());
        jsonUsers.setAttribute("password", st.nextToken());
        return jsonUsers.toString();
    }

    private static void postCreateUser(String jsonCreateUser, String host) throws IOException {

        AsyncHttpClient.BoundRequestBuilder request = client.preparePost("http://" + host + "/api/user");
        request.setBody(jsonCreateUser);
        request.execute(new AsyncCompletionHandler<Void>() {

            @Override
            public void onThrowable(Throwable t) {
                t.printStackTrace();
                errRequest.getAndIncrement();
            }

            @Override
            public Void onCompleted(Response response) throws Exception {
                userCreated.getAndIncrement();
                return null;
            }
        });
    }
}
