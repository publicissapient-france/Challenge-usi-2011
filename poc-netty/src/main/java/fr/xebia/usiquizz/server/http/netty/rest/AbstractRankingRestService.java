package fr.xebia.usiquizz.server.http.netty.rest;


import fr.xebia.usiquizz.core.game.Game;
import fr.xebia.usiquizz.core.game.Score;
import fr.xebia.usiquizz.core.game.Scoring;
import fr.xebia.usiquizz.core.persistence.Joueur;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class AbstractRankingRestService extends RestService {
    protected AbstractRankingRestService(Game game, Scoring scoring, ExecutorService executorService) {
        super(game, scoring, executorService);
    }

    protected ChannelBuffer constructJsonResponse(Score score, List<Joueur> top100, List<Joueur> prec, List<Joueur> suiv) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"score\":");
        sb.append("\"");
        sb.append(score.getCurrentScore());
        sb.append("\"");
        sb.append(",");
        sb.append("\"top_scores\":{");
        // boucle des top scores
        createJsonListJoueur(top100, sb);
        sb.append("},");

        sb.append("\"before\":{");

        // boucle des before scores
        createJsonListJoueur(prec, sb);


        sb.append("},");
        sb.append("\"after\":{");

        // boucle des after scores
        createJsonListJoueur(suiv, sb);


        sb.append("}");
        sb.append("}");
        ChannelBuffer cb = ChannelBuffers.dynamicBuffer(20000);
        cb.writeBytes(sb.toString().getBytes());
        return cb;
    }

    private void createJsonListJoueur(List<Joueur> list, StringBuilder sb) {
        int i = 1;
        StringBuilder sbMail = new StringBuilder();
        StringBuilder sbScores = new StringBuilder();
        StringBuilder sbFirstName = new StringBuilder();
        StringBuilder sbLastName = new StringBuilder();
        sbMail.append("\"mail\":[");
        sbScores.append("\"scores\":[");
        sbFirstName.append("\"firstname\":[");
        sbLastName.append("\"lastname\":[");
        for (Joueur j : list) {
            // Mail
            sbMail.append("\"");
            sbMail.append(j.getEmail());
            sbMail.append("\"");
            if (i < list.size()) {
                sbMail.append(",");
            }

            // scores
            sbScores.append("\"");
            sbScores.append(j.getScore());
            sbScores.append("\"");
            if (i < list.size()) {
                sbScores.append(",");
            }

            // firstname
            sbFirstName.append("\"");
            sbFirstName.append(j.getFirstName());
            sbFirstName.append("\"");
            if (i < list.size()) {
                sbFirstName.append(",");
            }

            // lastname
            sbLastName.append("\"");
            sbLastName.append(j.getLastName());
            sbLastName.append("\"");
            if (i < list.size()) {
                sbLastName.append(",");
            }

            i++;
        }
        sbMail.append("],");
        sbScores.append("],");
        sbFirstName.append("],");
        sbLastName.append("]");

        sb.append(sbMail.toString());
        sb.append(sbScores.toString());
        sb.append(sbFirstName.toString());
        sb.append(sbLastName.toString());
    }
}
