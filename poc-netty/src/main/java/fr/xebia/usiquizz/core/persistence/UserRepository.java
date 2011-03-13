package fr.xebia.usiquizz.core.persistence;

public interface UserRepository {
    String USER_COLLECTION_NAME = "USER";
    String EMAIL_FIELD = "email";
    String PASSWORD_FIELD = "password";
    String FIRSTNAME_FIELD = "firstname";
    String LASTNAME_FIELD = "lastname";

    void insertUser(String email, String password, String firstname,
            String lastname) throws UserAlreadyExists;

    User getUser(String mail);

    boolean checkUserWithEmailExist(String email);

    boolean logUser(String mail, String password);

}
