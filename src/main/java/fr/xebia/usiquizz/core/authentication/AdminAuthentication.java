package fr.xebia.usiquizz.core.authentication;

import java.util.ResourceBundle;

public class AdminAuthentication {

    public static String key;

    static {
        ResourceBundle rb = ResourceBundle.getBundle("configuration");
        key = rb.getString("adminAuthenticationkey");
    }

}
