package fr.xebia.usiquizz.core.jsonparsing;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.junit.Test;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class loginParseTest {

    JsonFactory jsonFactory = new JsonFactory();
    String login = "{\"email\":\"test.fdfdsfdsfs@test.Fr\", \"password\":\"testpassword\"}";
    private static final String JSON_MAIL = "email";
    private static final String JSON_PASSWORD = "password";

    @Test
    public void testJackonParsing() throws IOException {
        long start = System.nanoTime();

        for (int i = 0; i < 1000000; i++) {
            JsonParser jp = jsonFactory.createJsonParser(login);
            jp.nextToken(); // will return JsonToken.START_OBJECT (verify?)
            while (jp.nextToken() != JsonToken.END_OBJECT) {
                String fieldname = jp.getCurrentName();
                jp.nextToken(); // move to value, or START_OBJECT/START_ARRAY
                if (JSON_MAIL.equals(fieldname)) { // contains an object
                    jp.getText();
                } else if (JSON_PASSWORD.equals(fieldname)) {
                    jp.getText();
                }
            }
        }
        System.out.println("jackson take : " + (System.nanoTime() - start) / 1000000 + " ms");
    }

    @Test
    public void testRegexpParsing() {
        long start = System.nanoTime();
        Pattern pattern = Pattern.compile("\\{\"email\":\"(.*)\", \"password\":\"(.*)\"\\}");

        for (int i = 0; i < 1000000; i++) {
            Matcher m = pattern.matcher(login);
            if (m.matches()) {
                m.group(1);
                m.group(2);
            }
        }
        System.out.println("regexp take : " + (System.nanoTime() - start) / 1000000 + " ms");
    }

    @Test
    public void testCustomParsing() {
        long start = System.nanoTime();
        Pattern pattern = Pattern.compile("\\{\"email\":\"(.*)\", \"password\":\"(.*)\"\\}");

        for (int i = 0; i < 1000000; i++) {
            Matcher m = pattern.matcher(login);
            if (m.matches()) {
                m.group(1);
                m.group(2);
            }
        }
        System.out.println("regexp take : " + (System.nanoTime() - start) / 1000000 + " ms");
    }
}

