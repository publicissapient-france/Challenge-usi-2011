package fr.xebia.usiquizz.core.persistence;

import com.esotericsoftware.kryo.Kryo;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import fr.xebia.usiquizz.cache.CacheWrapper;
import fr.xebia.usiquizz.cache.NoCacheWrapper;

import java.nio.ByteBuffer;

public class MongoUserRepository extends AbstractRepository implements UserRepository {


    //private CacheWrapper<String, byte[]> cache = new EhCacheWrapper<String, byte[]>("User-cache", CacheManager.getInstance());
    private CacheWrapper<String, byte[]> cache = new NoCacheWrapper<String, byte[]>();


    public MongoUserRepository() {

        //DBCursor cursor = getDb().getCollection(USER_COLLECTION_NAME).find();
        //DBObject object = null;
        //System.out.println("Begin init cache");
        //while (cursor.hasNext()) {
        //    ByteBuffer buffer = ByteBuffer.allocateDirect(128);
        //    object = cursor.next();
        //    User user = new User((String) object.get(EMAIL_FIELD), (String) object.get(PASSWORD_FIELD), (String) object.get(FIRSTNAME_FIELD), (String) object.get(LASTNAME_FIELD));
        //    cache.put((String) object.get(EMAIL_FIELD), serializeUser(user));

        //}
        //System.out.println("Cache initialized");
    }

    @Override
    public void insertUser(String email, String password, String firstname, String lastname) throws UserAlreadyExists {
        // Check user doesn't exist
        if (checkUserWithEmailExist(email)) {
            throw new UserAlreadyExists(email);
        }
        BasicDBObject user = new BasicDBObject();
        user.put(EMAIL_FIELD, email);
        user.put(PASSWORD_FIELD, password);
        user.put(FIRSTNAME_FIELD, firstname);
        user.put(LASTNAME_FIELD, lastname);
        getDb().getCollection(USER_COLLECTION_NAME).insert(user);
        //byte[] bytes = serializeUser(new User(email, password, firstname, lastname));
        //cache.put(email, bytes);
    }



    @Override
    public User getUser(String mail) {
        byte[] serializedUser = null;
        DBObject object = null;
        if ((serializedUser = cache.get(mail)) == null) {
            BasicDBObject searchedUser = new BasicDBObject();
            searchedUser.put(EMAIL_FIELD, mail);
            DBCursor cursor = getDb().getCollection(USER_COLLECTION_NAME).find(searchedUser);
            if (cursor.hasNext()) {
                object = cursor.next();
                ByteBuffer buffer = ByteBuffer.allocateDirect(128);
                User user = new User((String) object.get(EMAIL_FIELD), (String) object.get(PASSWORD_FIELD), (String) object.get(FIRSTNAME_FIELD), (String) object.get(LASTNAME_FIELD));
                cache.put((String) object.get(EMAIL_FIELD), userSerializer.serializeUser(user));
                return user;
            } else {
                // No account
                return null;
            }
        } else {
            return userSerializer.deserializeUser(serializedUser);
        }
    }

    @Override
    public boolean checkUserWithEmailExist(String email) {
        // Don't use cache ... (mandatory if local cache)
        BasicDBObject searchedUser = new BasicDBObject();
        searchedUser.put(EMAIL_FIELD, email);
        DBCursor cursor = getDb().getCollection(USER_COLLECTION_NAME).find(searchedUser);
        if (cursor.hasNext()) {
            return true;
        }
        return false;
    }

    @Override
    public User logUser(String mail, String password) {
        BasicDBObject searchedUser = new BasicDBObject();
        searchedUser.put(EMAIL_FIELD, mail);
        searchedUser.put(PASSWORD_FIELD, password);
        if(getDb().getCollection(USER_COLLECTION_NAME).count(searchedUser) > 0)
            return new User();

        return null;
    }
}
