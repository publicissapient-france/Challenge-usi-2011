package fr.xebia.usiquizz.core.persistence;

import com.esotericsoftware.kryo.Kryo;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import fr.xebia.usiquizz.cache.CacheWrapper;
import fr.xebia.usiquizz.cache.EhCacheWrapper;
import fr.xebia.usiquizz.cache.NoCacheWrapper;
import net.sf.ehcache.CacheManager;

import java.nio.ByteBuffer;

public class UserRepository extends AbstractRepository {

    public static final String USER_COLLECTION_NAME = "USER";

    public static final String EMAIL_FIELD = "email";
    public static final String PASSWORD_FIELD = "password";
    public static final String FIRSTNAME_FIELD = "firstname";
    public static final String LASTNAME_FIELD = "lastname";

    private Kryo kryo = new Kryo();


    //private CacheWrapper<String, byte[]> cache = new EhCacheWrapper<String, byte[]>("User-cache", CacheManager.getInstance());
    private CacheWrapper<String, byte[]> cache = new NoCacheWrapper<String, byte[]>();


    public UserRepository() {
        kryo.register(User.class);
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

    public void insertUser(String email, String password, String firstname, String lastname) throws UserAlreadyExists {
        // Check user doesn't exist
        if(checkUserWithEmailExist(email)){
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

    private byte[] serializeUser(User user) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(128);
        kryo.writeObjectData(buffer, user);
        buffer.flip();
        byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes, 0, bytes.length);
        return bytes;
    }

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
                cache.put((String) object.get(EMAIL_FIELD), serializeUser(user));
                return user;
            } else {
                // No account
                return null;
            }
        } else {
            return kryo.readObjectData(ByteBuffer.wrap(serializedUser), User.class);
        }
    }

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

    public User logUser(String mail, String password) {
        User user = getUser(mail);
        if (user != null) {
            if (user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }
}
