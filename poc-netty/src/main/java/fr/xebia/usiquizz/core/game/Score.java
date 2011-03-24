package fr.xebia.usiquizz.core.game;

import java.util.Arrays;

public class Score {

    private byte currentScore;
    private boolean[] goodResponse;
    private boolean[] reponseFourni;

    public Score(byte nbQuestion) {
        currentScore = 0;
        goodResponse = new boolean[nbQuestion];
        reponseFourni = new boolean[nbQuestion];
        // init array
        Arrays.fill(goodResponse, false);
        Arrays.fill(reponseFourni, false);
    }

    public byte addResponse(boolean good, byte index) {
        // Manage first question = 0 internal
        index = (byte) (index - 1);
        byte newScore = currentScore;
        if (good) {
            // Selon l'index on ajoute le bon score
            if (index < 5) {
                newScore += 1;
            } else {
                newScore += (index / 5) * 5;
            }

            // Calcul du bonus

            byte currentIndex = (byte) (index - 1);
            while (currentIndex >= 0 && goodResponse[currentIndex]) {
                newScore += 1;
                currentIndex--;
            }
            goodResponse[index] = true;
            reponseFourni[index] = true;
        } else {
            goodResponse[index] = false;
            reponseFourni[index] = true;
        }
        this.currentScore = newScore;
        return newScore;
    }

    public byte getCurrentScore() {
        return currentScore;
    }

    public boolean[] getGoodResponse() {
        return goodResponse;
    }

    public boolean[] getReponseFourni() {
        return reponseFourni;
    }

    public boolean isAlreadyAnswer(byte currentQuestion) {
        // Manage difference between question index
        return reponseFourni[currentQuestion - 1];
    }
}
