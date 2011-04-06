package fr.xebia.usiquizz.core.game;

import fr.xebia.usiquizz.core.persistence.User;

import java.io.Serializable;
import java.util.Arrays;

public class Score implements Serializable {

    private byte currentScore;
    private byte[] reponse;
    private byte goodResponseCount;

    public final String email;
    public final String lname;
    public final String fname;

    public Score(byte nbQuestion, User user) {
        currentScore = 0;
        goodResponseCount = 0;
        reponse = new byte[nbQuestion];

        // init array
        Arrays.fill(reponse, (byte) 0);
        this.email = user.getMail();
        this.lname = user.getLastname();
        this.fname = user.getFirstname();
    }

    public byte addResponse(int choice, boolean good, byte index) {
        // Manage first question = 0 internal
        index = (byte) (index - 1);
        byte newScore = currentScore;

        // Reset responseCount if user did not answer last question
        if (index > 0 && reponse[index - 1] == 0) {
            goodResponseCount = 0;
        }

        if (good) {
            // Selon l'index on ajoute le bon score
            if (index < 5) {
                newScore += 1;
            } else {
                newScore += (index / 5) * 5;
            }

            // Calcul du bonus
            newScore += goodResponseCount;

            goodResponseCount++;

        } else {
            goodResponseCount = 0;
        }

        reponse[index] = (byte) choice;

        this.currentScore = newScore;
        return newScore;
    }

    public byte getCurrentScore() {
        return currentScore;
    }

    public byte[] getReponse() {
        return reponse;
    }


    public boolean isAlreadyAnswer(String currentQuestion) {
        // Manage difference between question index
        return reponse[Byte.parseByte(currentQuestion) - 1] != 0;
    }

    public boolean isAlreadyAnswer(int currentQuestion) {
        // Manage difference between question index
        return reponse[currentQuestion - 1] != 0;
    }

    
}
