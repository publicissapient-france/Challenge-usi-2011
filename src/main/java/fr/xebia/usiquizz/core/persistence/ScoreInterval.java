package fr.xebia.usiquizz.core.persistence;


public class ScoreInterval {

    private int scoreMin;

    private int scoreMax;

    public ScoreInterval(int scoreMin, int scoreMax) {
        this.scoreMin = scoreMin;
        this.scoreMax = scoreMax;
    }

    public int getScoreMin() {
        return scoreMin;
    }

    public int getScoreMax() {
        return scoreMax;
    }

    public boolean isInInterval(int score){
        return (score >= scoreMin) && (score <= scoreMax);
    }
}
