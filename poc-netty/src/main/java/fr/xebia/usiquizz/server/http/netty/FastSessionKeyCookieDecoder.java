package fr.xebia.usiquizz.server.http.netty;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FastSessionKeyCookieDecoder {

    private static final Pattern SESSION_KEY_PATTERN = Pattern.compile(".*session_key=(-?[0-9]*).*");

    public static String findSessionKey(String cookieString) {
        String sessionKey = null;
        Matcher m = SESSION_KEY_PATTERN.matcher(cookieString);
        if (m.matches()) {
            sessionKey = m.group(1);
        }
        return sessionKey;
    }

}
