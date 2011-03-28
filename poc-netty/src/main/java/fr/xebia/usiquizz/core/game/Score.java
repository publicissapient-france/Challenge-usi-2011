package fr.xebia.usiquizz.core.game;

import java.io.Serializable;
import java.util.Arrays;

public class Score implements Serializable {

    private byte currentScore;
    private byte[] reponse;
    private byte goodResponseCount;

    public Score(byte nbQuestion) {
        currentScore = 0;
        goodResponseCount = 0;
        reponse = new byte[nbQuestion];

        // init array
        Arrays.fill(reponse, (byte)0);
    }

    public byte addResponse(int choice, boolean good, byte index) {
        // Manage first question = 0 internal
        index = (byte) (index - 1);
        byte newScore = currentScore;

        // Reset responseCount if user did not answer last question
        if (index > 0 && reponse[index - 1] == 0){
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

        }else {
            goodResponseCount = 0;
        }

        reponse[index] = (byte)choice;

        this.currentScore = newScore;
        return newScore;
    }

    public byte getCurrentScore() {
        return currentScore;
    }

    public byte[] getReponse() {
        return reponse;
    }


    public boolean isAlreadyAnswer(byte currentQuestion) {
        // Manage difference between question index
        return reponse[currentQuestion - 1] != 0;
    }
}
