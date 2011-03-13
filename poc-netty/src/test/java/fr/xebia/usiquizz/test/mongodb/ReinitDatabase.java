package fr.xebia.usiquizz.test.mongodb;

import com.mongodb.BasicDBObject;
import fr.xebia.usiquizz.core.persistence.MongoUserRepository;
import fr.xebia.usiquizz.core.persistence.UserRepository;

import java.net.UnknownHostException;

public class ReinitDatabase {

    public static void main(String[] args) throws UnknownHostException {
        MongoUserRepository ur = new MongoUserRepository();
        ur.getDb().getCollection(UserRepository.USER_COLLECTION_NAME).drop();
        // Create index on user email
        ur.getDb().getCollection(UserRepository.USER_COLLECTION_NAME).ensureIndex(new BasicDBObject(UserRepository.EMAIL_FIELD, 1), "email_index");

    }

}
