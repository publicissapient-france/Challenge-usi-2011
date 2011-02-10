package fr.xebia.usiquizz.core.persistence;

import com.esotericsoftware.kryo.Kryo;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import fr.xebia.usiquizz.cache.CacheWrapper;
import fr.xebia.usiquizz.cache.EhCacheWrapper;
import net.sf.ehcache.CacheManager;

import java.nio.ByteBuffer;

public class UserRepository extends AbstractRepository {

    public static final String USER_COLLECTION_NAME = "USER";

    public static final String EMAIL_FIELD = "email";
    public static final String PASSWORD_FIELD = "password";
    public static final String FIRSTNAME_FIELD = "firstname";
    public static final String LASTNAME_FIELD = "lastname";

    private Kryo kryo = new Kryo();


    private CacheWrapper<String, byte[]> cache = new EhCacheWrapper<String, byte[]>("User-cache", CacheManager.getInstance());
    //private CacheWrapper<String, String> cache = new NoCacheWrapper<String, String>();


    public UserRepository() {
        kryo.register(User.class);
        DBCursor cursor = getDb().getCollection(USER_COLLECTION_NAME).find();
        DBObject object = null;
        System.out.println("Begin init cache");
        while (cursor.hasNext()) {
            ByteBuffer buffer = ByteBuffer.allocateDirect(256);
            object = cursor.next();
            kryo.writeObject(buffer, new User((String) object.get(EMAIL_FIELD), (String) object.get(PASSWORD_FIELD), (String) object.get(FIRSTNAME_FIELD), (String) object.get(LASTNAME_FIELD)));
            buffer.clear();
            byte[] bytes = new byte[buffer.capacity()];
            buffer.get(bytes, 0, bytes.length);
            cache.put((String) object.get(EMAIL_FIELD), bytes);

        }
        System.out.println("Cache initialized");
    }

    public void insertUser(String email, String password, String firstname, String lastname) {
        BasicDBObject user = new BasicDBObject();
        user.put(EMAIL_FIELD, email);
        user.put(PASSWORD_FIELD, password);
        user.put(FIRSTNAME_FIELD, firstname);
        user.put(LASTNAME_FIELD, lastname);
        getDb().getCollection(USER_COLLECTION_NAME).insert(user);
        ByteBuffer buffer = ByteBuffer.allocateDirect(256);
        kryo.writeObject(buffer, new User(email, password, firstname, lastname));
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes, 0, bytes.length);
        cache.put(email, bytes);
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
                ByteBuffer buffer = ByteBuffer.allocateDirect(256);
                kryo.writeObject(buffer, new User((String) object.get(EMAIL_FIELD), (String) object.get(PASSWORD_FIELD), (String) object.get(FIRSTNAME_FIELD), (String) object.get(LASTNAME_FIELD)));
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes, 0, bytes.length);
                cache.put((String) object.get(EMAIL_FIELD), bytes);
                return new User((String) object.get(EMAIL_FIELD), (String) object.get(PASSWORD_FIELD), (String) object.get(FIRSTNAME_FIELD), (String) object.get(LASTNAME_FIELD));

            }
            else {
                // No account
                return null;
            }
        }
        else {
            return kryo.readObject(ByteBuffer.wrap(serializedUser), User.class);
        }
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
