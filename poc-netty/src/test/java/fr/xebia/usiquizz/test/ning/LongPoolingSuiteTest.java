package fr.xebia.usiquizz.test.ning;

import com.ning.http.client.*;
import com.usi.Parametertype;
import com.usi.Question;
import com.usi.Questiontype;
import com.usi.Sessiontype;
import fr.xebia.usiquizz.core.xml.GameParameterParser;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
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
    private static final AtomicLong responseSended = new AtomicLong(0);
    private static final AtomicLong scoreReceived = new AtomicLong(0);
    private static final List<Future<Void>> futures = new ArrayList<Future<Void>>();
    private static long start;
    private static GameParameterParser gpp = new GameParameterParser();
    private static JsonFactory jf = new JsonFactory();

    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {

        if (args.length > 0) {
            host = args[0];
        } else {
            host = DEFAULT_HOST;
        }
        if (args.length > 1) {
            nbClient = Integer.parseInt(args[1]);
        }

        // First create database
        //HttpCreateUser.main(args);


        AsyncHttpClientConfig.Builder configBuilder = new AsyncHttpClientConfig.Builder();
        configBuilder.setMaximumConnectionsPerHost(100000);
        configBuilder.setMaximumConnectionsTotal(1000000);
        // Timeout 10 minutes
        configBuilder.setRequestTimeoutInMs(600000);
        configBuilder.setAllowPoolingConnection(false);
        AsyncHttpClient c = new AsyncHttpClient(configBuilder.build());

        // Init a game with NB_USER
        prepareGame(c);

        // Launch a game
        File loginFile = new File("src/test/test-file/1million_users_1.csv");
        BufferedReader loginReader = new BufferedReader(new FileReader(loginFile));
        // Skip firstline
        loginReader.readLine();
        start = System.nanoTime();
        for (int i = 0; i < nbClient; i++) {
            sendLoginRequest(c, "http://" + host + "/api/login", createJsonForLogin(loginReader.readLine()));
            nbRequestSend.incrementAndGet();
            if (i % 100 == 0) {
                System.out.println(i + " users send logging request");
            }
        }
        final long loggedTime = System.nanoTime();
        System.out.println("Take : " + ((double) (loggedTime - start)) / 1000000d + " ms for sending all request");
        loginReader.close();

    }

    private static String createJsonForLogin(String line) {
        StringTokenizer st = new StringTokenizer(line, ",", false);
        StringTemplate jsonUsers = new StringTemplate("{\"mail\":\"$mail$\",\"password\":\"$password$\"}", DefaultTemplateLexer.class);
        st.nextToken();
        st.nextToken();
        jsonUsers.setAttribute("mail", st.nextToken());
        jsonUsers.setAttribute("password", st.nextToken());
        return jsonUsers.toString();
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
        jgen.writeStringField("authentication_key", "xebia");
        jgen.writeFieldName("parameters");
        jgen.writeString(gpp.formatXmlParameter(generateSessionType(nbClient)));
        jgen.writeEndObject();
        jgen.close();

        return sw.getBuffer().toString().getBytes();
    }

    private static Sessiontype generateSessionType(int nbClient) {
        Sessiontype st = new Sessiontype();
        Parametertype pt = new Parametertype();
        Questiontype qt1 = new Questiontype();
        Questiontype qt2 = new Questiontype();
        Questiontype qt3 = new Questiontype();
        Question q1 = new Question();
        Question q2 = new Question();
        Question q3 = new Question();
        q1.setLabel("Quelle est la réponse à la question 1 ?");
        q2.setLabel("Quelle est la réponse à la question 2 ?");
        q3.setLabel("Quelle est la réponse à la question 3 ?");
        q1.getChoice().add("Reponse 1");
        q1.getChoice().add("Reponse 2");
        q1.getChoice().add("Reponse 3");
        q1.setGoodchoice(1);
        q2.getChoice().add("Reponse 1");
        q2.getChoice().add("Reponse 2");
        q2.getChoice().add("Reponse 3");
        q1.setGoodchoice(2);
        q3.getChoice().add("Reponse 1");
        q3.getChoice().add("Reponse 2");
        q3.getChoice().add("Reponse 3");
        q1.setGoodchoice(3);
        qt1.setQuestion(q1);
        qt2.setQuestion(q2);
        qt3.setQuestion(q3);
        st.getQuestions().add(qt1);
        st.getQuestions().add(qt2);
        st.getQuestions().add(qt3);

        pt.setFlushusertable(false);
        pt.setLongpollingduration(10000);
        pt.setNbquestions(3);
        pt.setNbusersthresold(nbClient);
        pt.setQuestiontimeframe(10000);
        pt.setTrackeduseridmail("");
        st.setParameters(pt);
        return st;
    }

    private static Future<Void> sendLoginRequest(final AsyncHttpClient c, String requestUrl, String body) throws IOException {
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

                c.getConfig().executorService().execute(new Runnable() {
                    @Override
                    public void run() {
                        LongPoolingSuiteTest.sendGetQuestionRequest(c, response);
                    }
                });

                return null;
            }

            @Override
            public void onThrowable(Throwable t) {
                t.printStackTrace();
            }
        });
        return null;
    }

    private static void sendGetQuestionRequest(final AsyncHttpClient client, Response response) {
        AsyncHttpClient.BoundRequestBuilder request = client.prepareGet("http://" + host + "/api/question/1");
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
                    } else if (nbRequestCompleted.get() % 100 == 0) {
                        final long end = System.nanoTime();
                        System.out.println(nbRequestCompleted.get() + " users ready in " + ((double) (end - start)) / 1000000d + " ms ");
                    }
                    client.getConfig().executorService().execute(new Runnable() {
                        @Override
                        public void run() {
                            LongPoolingSuiteTest.sendRandomResponse(client);
                        }
                    });

                    // Send response from question
                    return null;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendRandomResponse(AsyncHttpClient client) {
        AsyncHttpClient.BoundRequestBuilder request = client.preparePost("http://" + host + "/api/answer/1");
        request.setBody(createJsonRandomResponse());
        if (responseSended.incrementAndGet() % 100 == 0) {
            final long end = System.nanoTime();
            System.out.println(responseSended.get() + " answer sended in " + ((double) (end - start)) / 1000000d + " ms ");
        }
        try {
            request.execute(new AsyncCompletionHandler<Void>() {

                @Override
                public Void onCompleted(final Response response) throws Exception {
                    if (scoreReceived.incrementAndGet() % 100 == 0) {
                        final long end = System.nanoTime();
                        System.out.println(scoreReceived.get() + " score received in " + ((double) (end - start)) / 1000000d + " ms ");
                    }
                    return null;
                }

                @Override
                public void onThrowable(Throwable t) {
                    t.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] createJsonRandomResponse() {
        StringTemplate jsonAnswer = new StringTemplate("{\"answer\",$response$}", DefaultTemplateLexer.class);
        jsonAnswer.setAttribute("reponse", new Random().nextInt(2) + 1);
        return jsonAnswer.toString().getBytes();
    }
}