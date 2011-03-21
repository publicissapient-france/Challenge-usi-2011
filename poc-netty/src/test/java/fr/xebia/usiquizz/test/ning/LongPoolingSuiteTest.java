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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class LongPoolingSuiteTest {

    private static int nbClient = 1000;

    private static String host;
    private static final String DEFAULT_HOST = "127.0.0.1:8080";

    private static final AtomicLong nbRequestSend = new AtomicLong(0);
    private static final AtomicLong nbLogin = new AtomicLong(0);
    private static final AtomicLong nbQuestionRequested = new AtomicLong(0);
    private static final AtomicLong nbRequestCompleted = new AtomicLong(0);
    private static final AtomicLong responseSended = new AtomicLong(0);
    private static final AtomicLong scoreReceived = new AtomicLong(0);

    // Counter for audit
    private static final AtomicLong loginCounter = new AtomicLong(0);
    private static final AtomicLong questionCounter = new AtomicLong(0);
    private static final AtomicLong answerCounter = new AtomicLong(0);

    private static final Map<String, Long> loginCallAudit = new ConcurrentHashMap<String, Long>();
    private static final Map<String, Long> questionCallAudit = new ConcurrentHashMap<String, Long>();
    private static final Map<String, Long> answerCallAudit = new ConcurrentHashMap<String, Long>();

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
        configBuilder.setRequestTimeoutInMs(6000000);
        configBuilder.setAllowPoolingConnection(true);
        AsyncHttpClient c = new AsyncHttpClient(configBuilder.build());

        // Init a game with NB_USER
        prepareGame(c);

        // Launch a game
        File loginFile = new File("src/test/test-file/1million_users_1.csv");
        BufferedReader loginReader = new BufferedReader(new FileReader(loginFile));
        // Skip firstline
        loginReader.readLine();
        start = System.nanoTime();
        nbRequestSend.set(0);
        nbLogin.set(0);
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

    private static void writeAudit() {
        long loginTotalTime = 0;
        long loginMaxTime = 0;
        for (long time : loginCallAudit.values()) {
            loginTotalTime += time;
            if (time > loginMaxTime)
                loginMaxTime = time;
        }
        long questionTotalTime = 0;
        long questionMaxTime = 0;
        for (long time : questionCallAudit.values()) {
            questionTotalTime += time;
            if (time > questionMaxTime)
                questionMaxTime = time;
        }
        long answerTotalTime = 0;
        long answerMaxTime = 0;
        for (long time : answerCallAudit.values()) {
            answerTotalTime += time;
            if (time > answerMaxTime)
                answerMaxTime = time;
        }
        System.out.println("Login mean time : " + ((double) loginTotalTime / (double) loginCallAudit.size()));
        System.out.println("Login max time : " + loginMaxTime);
        System.out.println("Question mean time : " + ((double) questionTotalTime / (double) questionCallAudit.size()));
        System.out.println("Question max time : " + questionMaxTime);
        System.out.println("Answer mean time : " + ((double) answerTotalTime / (double) answerCallAudit.size()));
        System.out.println("Answer max time : " + answerMaxTime);
    }

    private static void prepareGame(final AsyncHttpClient c) throws IOException, ExecutionException, InterruptedException {
        AsyncHttpClient.BoundRequestBuilder request = c.preparePost("http://" + host + "/api/game");
        request.setBody(createJsonGame(nbClient));
        request.execute().get();
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
        request.execute(new AsyncAuditedCompletionHandler<Void>("login_" + loginCounter.getAndIncrement(), System.nanoTime()) {

            @Override
            public Void onCompleted(final Response response, final String id) throws Exception {
                loginCallAudit.put(id, getRequestTimeInMillis());
                // Once logged get first question
                if (nbLogin.incrementAndGet() == nbClient) {
                    final long end = System.nanoTime();
                    System.out.println("Take : " + ((double) (end - start)) / 1000000d + " ms for loging all users");
                }

                c.getConfig().executorService().execute(new Runnable() {
                    @Override
                    public void run() {
                        LongPoolingSuiteTest.sendGetQuestionRequest(c, response, "1");
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

    private static void sendGetQuestionRequest(final AsyncHttpClient client, Response response, final String questionNumber) {
        AsyncHttpClient.BoundRequestBuilder request = client.prepareGet("http://" + host + "/api/question/" + questionNumber);
        for (Cookie c : response.getCookies()) {
            request.addCookie(c);
        }

        try {
            nbQuestionRequested.incrementAndGet();
            if (nbQuestionRequested.get() % 100 == 0) {
                final long end = System.nanoTime();
                System.out.println(nbQuestionRequested.get() + " question:" + questionNumber + " requested " + ((double) (end - start)) / 1000000d + " ms ");
            }
            // Last client request question...
            // Reset counter
            if (nbQuestionRequested.get() == nbClient) {
                responseSended.set(0);
                scoreReceived.set(0);
                nbQuestionRequested.set(0);
            }
            request.execute(new AsyncAuditedCompletionHandler<Void>("question_" + questionCounter.getAndIncrement(), System.nanoTime()) {

                @Override
                public void onThrowable(Throwable t) {
                    t.printStackTrace();
                }

                @Override
                public Void onCompleted(final Response response, final String id) throws Exception {
                    questionCallAudit.put(id, getRequestTimeInMillis());
                    if (nbRequestCompleted.incrementAndGet() == nbClient) {
                        final long end = System.nanoTime();
                        System.out.println("Take : " + ((double) (end - start)) / 1000000d + " ms for complete long pooling");
                        nbRequestCompleted.set(0);
                    } else if (nbRequestCompleted.get() % 100 == 0) {
                        final long end = System.nanoTime();
                        System.out.println(nbRequestCompleted.get() + " users ready in " + ((double) (end - start)) / 1000000d + " ms ");
                    }
                    client.getConfig().executorService().execute(new Runnable() {
                        @Override
                        public void run() {
                            LongPoolingSuiteTest.sendRandomResponse(client, response, questionNumber);
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

    private static void sendRandomResponse(final AsyncHttpClient client, Response response, final String questionNumber) {
        AsyncHttpClient.BoundRequestBuilder request = client.preparePost("http://" + host + "/api/answer/" + questionNumber);
        for (Cookie c : response.getCookies()) {
            request.addCookie(c);
        }
        request.setBody(createJsonRandomResponse());
        if (responseSended.incrementAndGet() % 100 == 0) {
            final long end = System.nanoTime();
            System.out.println(responseSended.get() + " answer sended in " + ((double) (end - start)) / 1000000d + " ms ");
        }
        try {
            request.execute(new AsyncAuditedCompletionHandler<Void>("answer_" + answerCounter.getAndIncrement(), System.nanoTime()) {

                @Override
                public Void onCompleted(final Response response, final String id) throws Exception {
                    answerCallAudit.put(id, getRequestTimeInMillis());
                    if (scoreReceived.incrementAndGet() % 100 == 0) {
                        final long end = System.nanoTime();
                        System.out.println(scoreReceived.get() + " score received in " + ((double) (end - start)) / 1000000d + " ms ");
                    }

                    // Si il reste des questions on recommence
                    if (Integer.parseInt(questionNumber) < 2) {
                        client.getConfig().executorService().execute(new Runnable() {
                            @Override
                            public void run() {
                                LongPoolingSuiteTest.sendGetQuestionRequest(client, response, Integer.toString(Integer.parseInt(questionNumber) + 1));
                            }
                        });
                    } else if (scoreReceived.get() == nbClient) {
                        writeAudit();
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
        StringBuilder sb = new StringBuilder();
        sb.append("{\"answer\":");
        sb.append(new Random().nextInt(2) + 1);
        sb.append("}");
        return sb.toString().getBytes();
    }

    private static String createJsonForLogin(String line) {
        StringTokenizer st = new StringTokenizer(line, ",", false);
        st.nextToken();
        st.nextToken();
        StringBuilder sb = new StringBuilder();
        sb.append("{\"mail\":\"");
        sb.append(st.nextToken());
        sb.append("\",\"password\":\"");
        sb.append(st.nextToken());
        sb.append("\"}");
        return sb.toString();
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
}