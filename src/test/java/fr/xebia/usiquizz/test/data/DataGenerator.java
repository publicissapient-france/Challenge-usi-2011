package fr.xebia.usiquizz.test.data;

import org.apache.commons.lang.RandomStringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DataGenerator {

    public static void main(String args[]) throws IOException {

        File loginFile = new File("src/test/test-file/logins.json");
        File userFile = new File("src/test/test-file/users.json");

        if (loginFile.exists()) {
            loginFile.delete();
        }
        loginFile.createNewFile();

        if (userFile.exists()) {
            userFile.delete();
        }
        userFile.createNewFile();

        BufferedWriter loginWriter = new BufferedWriter(new FileWriter(loginFile));
        BufferedWriter userWriter = new BufferedWriter(new FileWriter(userFile));

        for (int i = 0; i < 100000; i++) {
            String firstname = generate();
            String lastname = generate();
            String password = generate();
            String email = firstname + "@test.org";
            loginWriter.write("{\"mail\": \"" + email + "\", \"password\": \"" + password + "\"}");
            loginWriter.newLine();
            userWriter.write("{\"mail\": \"" + email + "\", \"password\": \"" + password + "\", \"firstname\": \"" + firstname + "\" ,\"lastname\": \"" + lastname + "\" }");
            userWriter.newLine();
        }

        loginWriter.close();
        userWriter.close();
    }

    private static String generate() {
        return RandomStringUtils.randomAlphabetic(8);
    }

}
