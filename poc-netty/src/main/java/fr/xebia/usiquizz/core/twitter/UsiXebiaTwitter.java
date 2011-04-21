package fr.xebia.usiquizz.core.twitter;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

public class UsiXebiaTwitter {

    private static final String oauthToken = "282079867-CdPeDbQYiuIWp4AV2VE1iq8PDesPgV47wHRloccQ";
    private static final String oauthSecret = "3ig48nDFZmKbZFmVRQzt4dBhve1BzV8pH17aRwh5OA";

    private static final Logger logger = LoggerFactory.getLogger(UsiXebiaTwitter.class);

    private TwitterFactory twitterFactory;

    public UsiXebiaTwitter() {
        twitterFactory = new TwitterFactory();
    }

    public void tweetNbUserSupportedByAppli(int nbJoueur) {
        logger.info("Tweet this message : " + "Notre appli supporte " + nbJoueur + " joueurs #challengeUSI2011");
                        
        if (System.getProperty("tweetAfterRanking") != null && System.getProperty("tweetAfterRanking").equals("true")) {
            Twitter twitter = twitterFactory.getInstance(new AccessToken(oauthToken, oauthSecret));
            try {
                twitter.updateStatus("Notre appli supporte " + nbJoueur + " joueurs #challengeUSI2011");
            } catch (TwitterException e) {
                logger.error("Cannot send tweet ", e);
            }
        }
    }

}
