package fr.xebia.usiquizz.server.http.netty.rest;


import com.usi.Question;
import fr.xebia.usiquizz.core.authentication.AdminAuthentication;
import fr.xebia.usiquizz.core.game.Game;
import fr.xebia.usiquizz.core.game.Scoring;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class JsonAuditAnswerRestService extends RestService {

    private static final String AUTHENTICATION_KEY = "authentication_key";
    private static final String USER_MAIL = "user_mail";

    private static Logger logger = LoggerFactory.getLogger(JsonAuditAnswerRestService.class);


    public JsonAuditAnswerRestService(Game game, Scoring scoring, ExecutorService executorService) {
        super(game, scoring, executorService);
    }

    @Override
    public void get(String path, ChannelHandlerContext ctx, MessageEvent e) {
        try {
            HttpRequest request = (HttpRequest) e.getMessage();
            // Get authentication_key
            String authenticationKey = "";
            String userMail = "";
            QueryStringDecoder qsd = new QueryStringDecoder(request.getUri());
            List<String> auths = qsd.getParameters().get(AUTHENTICATION_KEY);
            if (auths != null && auths.size() > 0) {
                authenticationKey = auths.get(0);
            }
            List<String> mails = qsd.getParameters().get(USER_MAIL);
            if (mails != null && mails.size() > 0) {
                userMail = mails.get(0);
            }

            if (authenticationKey == null || !authenticationKey.equals(AdminAuthentication.key)) {
                logger.info("User with bad authentication ");
                responseWriter.writeResponse(HttpResponseStatus.UNAUTHORIZED, ctx, e);
                return;
            }

            // On regarde si on demande une question en particulier
            String requestPath = qsd.getPath();

            String fakeSessionKey = Integer.valueOf(userMail.hashCode()).toString();


            try {
                byte questionNbr = Byte.parseByte(requestPath.substring(requestPath.lastIndexOf("/") + 1));

                // Fail on bad question number
                if (questionNbr < 1 || questionNbr > game.getNbquestions()) {
                    responseWriter.writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
                    return;
                }

                responseWriter.writeResponse(sendQuestionResult(game.getQuestion(questionNbr), scoring.getAnswers(fakeSessionKey)[questionNbr - 1]), HttpResponseStatus.OK, ctx, e, null);
                return;
            } catch (NumberFormatException ex) {
            }

            responseWriter.writeResponse(sendAllQuestionResult(scoring.getAnswers(fakeSessionKey), game.getQuestionList()), HttpResponseStatus.OK, ctx, e, null);
            return;

        } catch (Exception exc) {
            logger.error("error during question rest service", exc);
            responseWriter.writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
        }
    }


    private ChannelBuffer sendQuestionResult(Question question, byte uResponse) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"user_answer\":\"");
        sb.append(uResponse);
        sb.append("\",\"good_answer\":\"");
        sb.append(question.getGoodchoice());
        sb.append("\",\"question\":\"").append(question.getLabel()).append("\"}");
        ChannelBuffer cb = ChannelBuffers.dynamicBuffer(256);
        cb.writeBytes(sb.toString().getBytes());
        return cb;
    }

    private ChannelBuffer sendAllQuestionResult(byte[] uResponse, List<Question> questionList) {
        StringBuilder sb = new StringBuilder("{\"user_answers\":[");
        StringBuilder sbg = new StringBuilder("[");
        int i = 0;
        while (i < uResponse.length) {

            if (i > 0) {
                sb.append(',');
                sbg.append(',');
            }
            sb.append("\"");
            sb.append(uResponse[i]);
            sb.append("\"");

            sbg.append("\"");
            sbg.append(questionList.get(i).getGoodchoice());
            sbg.append("\"");
            i++;
        }
        sb.append("],\"good_answers\":").append(sbg).append("]}");
        ChannelBuffer cb = ChannelBuffers.dynamicBuffer(1024);
        cb.writeBytes(sb.toString().getBytes());
        return cb;
    }
}
