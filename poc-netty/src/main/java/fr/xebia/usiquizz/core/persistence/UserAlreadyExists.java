package fr.xebia.usiquizz.core.persistence;

public class UserAlreadyExists extends RuntimeException {

    private String email;

    public UserAlreadyExists(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
