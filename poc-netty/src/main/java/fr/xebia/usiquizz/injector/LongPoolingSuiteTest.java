package fr.xebia.usiquizz.injector;

import com.ning.http.client.*;
import com.ning.http.client.providers.netty.NettyAsyncHttpProviderConfig;
import com.usi.Parametertype;
import com.usi.Question;
import com.usi.Questiontype;
import com.usi.Sessiontype;
import fr.xebia.usiquizz.core.xml.GameParameterParser;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPInputStream;

public class LongPoolingSuiteTest {

    private static final Logger logger = LoggerFactory.getLogger(LongPoolingSuiteTest.class);

    private static String host;
    private static final String DEFAULT_HOST = "127.0.0.1:8080";
    private static int nbClient;

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

    private static long start;
    private static GameParameterParser gpp = new GameParameterParser();
    private static JsonFactory jf = new JsonFactory();
    private static Sessiontype session;

    private static String userFile = "src/test/test-file/1million_users_1.csv.gz";
    private static String gameFile = "src/test/test-file/game.xml";
    private static String gameContent;

    public static void main(String[] args) throws Exception, InterruptedException, IOException {

        if (args.length > 0) {
            host = args[0];
        } else {
            host = DEFAULT_HOST;
        }
        if (args.length > 1) {
            userFile = args[1];
        }

        if (args.length > 2) {
            try {
                gameFile = args[2];
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // First create database
        //HttpCreateUser.main(args);


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
        AsyncHttpClient c = new AsyncHttpClient(configBuilder.build());
        gameContent = loadContent();
        session = new GameParameterParser().parseXmlParameter(gameContent);
        nbClient = session.getParameters().getNbusersthreshold();
        // Init a game with NB_USER
        prepareGame(c);

        // Launch a game
        File loginFile = new File(userFile);
        BufferedReader loginReader;
        if (userFile.endsWith(".gz")) {
            loginReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(userFile))));
        } else {
            loginReader = new BufferedReader(new FileReader(userFile));
        }

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
        request.setBody(createJsonGame());
        request.execute().get();
    }


    private static Future<Void> sendLoginRequest(final AsyncHttpClient c, final String requestUrl, final String body) {
        try {
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
                public void onThrowable(Throwable t, final String id) {
                    logger.error("Error in login request", t);
                    // Try again
                    //LongPoolingSuiteTest.sendLoginRequest(c, requestUrl, body);
                }
            });
        } catch (IOException e) {
            logger.error("No recoverable error in login", e);
        }
        return null;
    }

    private static void sendGetQuestionRequest(final AsyncHttpClient client, final Response response, final String questionNumber) {
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
                public void onThrowable(Throwable t, final String id) {
                    logger.error("Error in get question request", t);
                    // Try again
                    //LongPoolingSuiteTest.sendGetQuestionRequest(client, response, questionNumber);
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
                    if (Integer.parseInt(questionNumber) < session.getParameters().getNbquestions()) {
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
                public void onThrowable(Throwable t, final String id) {
                    logger.error("Error send response request", t);
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

    private static String loadContent() throws Exception {
        int len;
        char[] chr = new char[4096];
        final StringBuffer buffer = new StringBuffer();
        final FileReader reader = new FileReader(gameFile);
        try {
            while ((len = reader.read(chr)) > 0) {
                buffer.append(chr, 0, len);
            }
        } finally {
            reader.close();
        }
        return buffer.toString();
    }

    private static byte[] createJsonGame() throws IOException {


        StringWriter sw = new StringWriter();
        JsonGenerator jgen = jf.createJsonGenerator(sw);

        jgen.writeStartObject();
        jgen.writeStringField("authentication_key", "xebia");
        jgen.writeFieldName("parameters");
        jgen.writeString(gameContent);
        jgen.writeEndObject();
        jgen.close();

        return sw.getBuffer().toString().getBytes();
    }
}