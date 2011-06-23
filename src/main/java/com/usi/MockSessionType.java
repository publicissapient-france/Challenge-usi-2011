package com.usi;

public class MockSessionType extends Sessiontype {

    private Questiontype qt;

    public MockSessionType() {
        qt = new Questiontype();
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
        qt.getQuestion().add(q1);
        qt.getQuestion().add(q2);
        qt.getQuestion().add(q3);
    }

    @Override
    public Parametertype getParameters() {
        return new MockParameter();
    }

    @Override
    public Questiontype getQuestions() {
        return qt;
    }
}

class MockParameter extends Parametertype {

    public int getLongpollingduration() {
        return 10000;
    }

    public int getNbusersthresold() {
        return 5000;
    }

    public int getQuestiontimeframe() {
        return 10;
    }

    public int getNbquestions() {
        return 1;
    }

    public boolean isFlushusertable() {
        return true;
    }

    public String getTrackeduseridmail() {
        return "";
    }
}
