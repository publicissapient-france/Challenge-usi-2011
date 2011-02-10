package fr.xebia.usiquizz.test.ning;

import com.ning.http.client.*;
import com.usi.Parametertype;
import com.usi.Question;
import com.usi.Questiontype;
import com.usi.Sessiontype;
import fr.xebia.usiquizz.core.xml.GameParameterParser;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

public class LongPoolingSuiteTest {

    private static int nbClient = 5000;

    private static String host;
    private static final String DEFAULT_HOST = "127.0.0.1:8080";

    private static final AtomicLong nbRequestSend = new AtomicLong(0);
    private static final AtomicLong nbLogin = new AtomicLong(0);
    private static final AtomicLong nbRequestCompleted = new AtomicLong(0);
    private static final List<Future<Void>> futures = new ArrayList<Future<Void>>();
    private static long start;
    private static GameParameterParser gpp = new GameParameterParser();
    private static JsonFactory jf = new JsonFactory();

    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        // First create database
        if (args.length > 0) {
            host = args[0];
        }
        else {
            host = DEFAULT_HOST;
        }
        if (args.length > 1) {
            nbClient = Integer.parseInt(args[1]);
        }
        //HttpCreateUser.main(args);


        AsyncHttpClientConfig.Builder configBuilder = new AsyncHttpClientConfig.Builder();
        configBuilder.setMaximumConnectionsPerHost(100000);
        configBuilder.setMaximumConnectionsTotal(1000000);
        // Timeout 10 minutes
        configBuilder.setRequestTimeoutInMs(600000);
        AsyncHttpClient c = new AsyncHttpClient(configBuilder.build());

        // Init a game with NB_USER
        prepareGame(c);

        // Launch a game
        File loginFile = new File("src/test/test-file/logins.json");
        BufferedReader loginReader = new BufferedReader(new FileReader(loginFile));
        start = System.nanoTime();
        for (int i = 0; i < nbClient; i++) {
            sendRequest(c, "http://" + host + "/api/login", loginReader.readLine());
            nbRequestSend.incrementAndGet();
        }
        final long loggedTime = System.nanoTime();
        System.out.println("Take : " + ((double) (loggedTime - start)) / 1000000d + " ms for sending all request");
        loginReader.close();

    }

    private static void prepareGame(final AsyncHttpClient c) throws IOException, ExecutionException, InterruptedException {
        AsyncHttpClient.BoundRequestBuilder request = c.preparePost("http://" + host + "/api/game");
        request.setBody(createJsonGame(nbClient));
        request.execute().get();
    }

    private static byte[] createJsonGame(int nbClient) throws IOException {
        StringWriter sw = new StringWriter();
        JsonGenerator jgen = jf.createJsonGenerator(sw);

        jgen.writeStartObject();
        jgen.writeStringField("authentication_key", "key");
        jgen.writeFieldName("parameters");
        jgen.writeString(gpp.formatXmlParameter(generateSessionType(nbClient)));
        jgen.writeEndObject();
        jgen.close();

        return sw.getBuffer().toString().getBytes();
    }

    private static Sessiontype generateSessionType(int nbClient) {
        Sessiontype st = new Sessiontype();
        Parametertype pt = new Parametertype();
        Question q = new Question();
        q.setLabel("Question 1");
        q.getChoice().add("Reponse 1");
        q.getChoice().add("Reponse 2");
        q.getChoice().add("Reponse 3");
        q.getChoice().add("Reponse 4");
        q.setGoodchoice(2);
        Questiontype qt = new Questiontype();
        qt.setQuestion(q);
        st.getQuestions().add(qt);

        pt.setFlushusertable(false);
        pt.setLongpollingduration(10000);
        pt.setNbquestions(1);
        pt.setNbusersthresold(nbClient);
        pt.setQuestiontimeframe(10000);
        pt.setTrackeduseridmail("");
        st.setParameters(pt);
        return st;
    }

    private static Future<Void> sendRequest(final AsyncHttpClient c, String requestUrl, String body) throws IOException {
        AsyncHttpClient.BoundRequestBuilder request = c.preparePost(requestUrl);
        request.setBody(body);
        request.execute(new AsyncCompletionHandler<Void>() {

            @Override
            public Void onCompleted(final Response response) throws Exception {
                // Once logged get first question
                if (nbLogin.incrementAndGet() == nbClient) {
                    final long end = System.nanoTime();
                    System.out.println("Take : " + ((double) (end - start)) / 1000000d + " ms for loging all users");
                }
                // Cookie management
                c.getConfig().executorService().execute(new Runnable() {
                    @Override
                    public void run() {
                        AsyncHttpClient.BoundRequestBuilder request = c.prepareGet("http://" + host + "/api/question/1");
                        for (Cookie c : response.getCookies()) {
                            request.addCookie(c);
                        }

                        try {
                            request.execute(new AsyncHandler<Void>() {

                                @Override
                                public void onThrowable(Throwable t) {
                                    t.printStackTrace();
                                }

                                @Override
                                public STATE onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
                                    return STATE.CONTINUE;
                                }

                                @Override
                                public STATE onStatusReceived(HttpResponseStatus responseStatus) throws Exception {
                                    return STATE.CONTINUE;
                                }

                                @Override
                                public STATE onHeadersReceived(HttpResponseHeaders headers) throws Exception {
                                    return STATE.CONTINUE;
                                }

                                @Override
                                public Void onCompleted() throws Exception {
                                    if (nbRequestCompleted.incrementAndGet() == nbClient) {
                                        final long end = System.nanoTime();
                                        System.out.println("Take : " + ((double) (end - start)) / 1000000d + " ms for complete long pooling");
                                    }
                                    else if (nbRequestCompleted.get() % 100 == 0) {
                                        final long end = System.nanoTime();
                                        System.out.println(nbRequestCompleted.get() + " users ready in " + ((double) (end - start)) / 1000000d + " ms ");
                                    }
                                    return null;
                                }
                            });
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

                return null;
            }

            @Override
            public void onThrowable(Throwable t) {
                // Something wrong happened.
            }
        });
        return null;
    }


}
