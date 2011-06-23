package fr.xebia.usiquizz.core.persistence;

public interface GemfireAttribute {

    // PARAMETRE DU JEU
    public static final String LOGIN_TIMEOUT = "login-timeout";
    public static final String SYNCHROTIME = "synchrotime";
    public static final String NB_USERS_THRESOLD = "nb-users-thresold";
    public static final String QUESTION_TIME_FRAME = "question-time-frame";
    public static final String NB_QUESTIONS = "nb-questions";
    public static final String FLUSH_USER_TABLE = "flush-user-table";
    public static final String TRACKED_USER_IDMAIL = "tracked-user-idmail";

    // QUESTION
    public static final String QUESTION_LIST = "question_list";
    // Response
    public static final String GOOD_RESPONSE = "good-response";


    // ETAT COURANT DU JEU
    public static final String CURRENT_QUESTION_INDEX = "current-question-index";
    public static final String CURRENT_ANSWER_INDEX = "current-answer-index";
    public static final String LOGIN_PHASE_STATUS = "login-phase-status";

    // Etat du login
    public static final byte LOGIN_PHASE_NON_COMMENCER = 21;
    public static final byte LOGIN_PHASE_EN_COURS = 22;
    public static final byte LOGIN_PHASE_TERMINER = 23;

}
