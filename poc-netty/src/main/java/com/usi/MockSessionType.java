package com.usi;

public class MockSessionType extends Sessiontype {
    public Parametertype getParameters() {
        return new MockParameter();
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
