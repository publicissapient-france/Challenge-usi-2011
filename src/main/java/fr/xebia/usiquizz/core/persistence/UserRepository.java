package fr.xebia.usiquizz.core.persistence;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import fr.xebia.usiquizz.cache.EhCacheWrapper;
import net.sf.ehcache.CacheManager;

public class UserRepository extends AbstractRepository {

    public static final String USER_COLLECTION_NAME = "USER";

    public static final String EMAIL_FIELD = "email";
    public static final String PASSWORD_FIELD = "password";
    public static final String FIRSTNAME_FIELD = "firstname";
    public static final String LASTNAME_FIELD = "lastname";

    private EhCacheWrapper<String, String> inProcessCache = new EhCacheWrapper<String, String>("User-cache", CacheManager.getInstance());

    public void insertUser(String email, String password, String firstname, String lastname) {
        BasicDBObject user = new BasicDBObject();
        user.put(EMAIL_FIELD, email);
        user.put(PASSWORD_FIELD, password);
        user.put(FIRSTNAME_FIELD, firstname);
        user.put(LASTNAME_FIELD, lastname);
        getDb().getCollection(USER_COLLECTION_NAME).insert(user);
        inProcessCache.put(email, JSON.serialize(user));
    }

    public User getUser(String mail) {
        String stringObject = null;
        DBObject object = null;
        if ((stringObject = inProcessCache.get(mail)) == null) {
            BasicDBObject searchedUser = new BasicDBObject();
            searchedUser.put(EMAIL_FIELD, mail);
            DBCursor cursor = getDb().getCollection(USER_COLLECTION_NAME).find(searchedUser);
            if (cursor.hasNext()) {
                object = cursor.next();
                inProcessCache.put(mail, JSON.serialize(object));
            }else{
                // No account
                return null;
            }
        }
        else {
            object = (DBObject) JSON.parse(stringObject);
        }
        return new User((String) object.get(EMAIL_FIELD), (String) object.get(PASSWORD_FIELD), (String) object.get(FIRSTNAME_FIELD), (String) object.get(LASTNAME_FIELD));
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
