package fr.xebia.usiquizz.core.persistence;


import java.util.Comparator;

public class Joueur {
    private String lastName;
    private String firstName;
    private String email;

    public Joueur(String lastName, String firstName, String email) {
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
}

class JoueurComparator implements Comparator<Joueur> {

    @Override
    public int compare(Joueur o1, Joueur o2) {
        int comp;
        if ((comp = o1.getEmail().compareTo(o2.getEmail())) == 0) {
            if ((comp = o1.getLastName().compareTo(o2.getLastName())) == 0) {
                return o1.getFirstName().compareTo(o2.getFirstName());
            }
        }
        return comp;
    }
}

