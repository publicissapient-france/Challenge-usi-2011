package com.usi;

import java.util.ArrayList;
import java.util.List;

public class MockSessionType extends Sessiontype {

    private List<Questiontype> questionsList;

    public MockSessionType(){
        questionsList = new ArrayList<Questiontype>();
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
        questionsList.add(qt1);
        questionsList.add(qt2);
        questionsList.add(qt3);
    }

    public Parametertype getParameters() {
        return new MockParameter();
    }

    public List<Questiontype> getQuestions() {
        return questionsList;
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
