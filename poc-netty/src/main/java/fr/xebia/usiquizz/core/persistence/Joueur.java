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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Joueur joueur = (Joueur) o;

        if (score != joueur.score) return false;
        if (email != null ? !email.equals(joueur.email) : joueur.email != null) return false;
        if (firstName != null ? !firstName.equals(joueur.firstName) : joueur.firstName != null) return false;
        if (lastName != null ? !lastName.equals(joueur.lastName) : joueur.lastName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = score;
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        return result;
    }
}