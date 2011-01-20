package fr.xebia.usiquizz.core.persistence;

public class User {
    private String mail;
    private String password;
    private String firstname;
    private String lastname;

    public User(String mail, String password, String firstname, String lastname) {
        this.mail = mail;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;
    }

    public String getMail() {
        return mail;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }


    public String toJsonString() {
        StringBuffer sb = new StringBuffer();
        sb.append("{\"mail\":\"");
        sb.append(mail);
        sb.append("\",\"password\":\"");
        sb.append(password);
        sb.append("\",\"firsname\":\"");
        sb.append(firstname);
        sb.append("\",\"lastname\":\"");
        sb.append(lastname);
        sb.append("\"}");
        return sb.toString();
    }
}
