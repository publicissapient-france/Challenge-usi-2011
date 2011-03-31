package fr.xebia.usiquizz.core.persistence;


import java.io.Serializable;
import java.util.Comparator;

public class Joueur implements Serializable, Comparable<Joueur> {
    private int score;
    private String lastName;
    private String firstName;
    private String email;

    public Joueur(int score, String lastName, String firstName, String email) {
        this.score = score;
        this.lastName = lastName;
        this.firstName = firstName;
        this.email = email;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getEmail() {
        return email;
    }

    public int getScore() {
        return score;
    }

    @Override
    public int compareTo(Joueur o1) {
        int comp;
        if ((comp = this.getScore() - o1.getScore()) == 0) {
            if ((comp = this.getEmail().compareTo(o1.getEmail())) == 0) {
                if ((comp = this.getLastName().compareTo(o1.getLastName())) == 0) {
                    return this.getFirstName().compareTo(o1.getFirstName());
                }
            }
        }
        return comp;
    }

}