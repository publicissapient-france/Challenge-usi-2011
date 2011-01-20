package fr.xebia.usiquizz.test.netty;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.Mongo;
import fr.xebia.usiquizz.core.persistence.UserRepository;

import java.net.UnknownHostException;

public class ReinitDatabase {

    public static void main(String[] args) throws UnknownHostException {
        UserRepository ur = new UserRepository();
        // Create index on user email
        ur.getDb().getCollection(UserRepository.USER_COLLECTION_NAME).ensureIndex(new BasicDBObject(UserRepository.EMAIL_FIELD, 1));

    }

}
