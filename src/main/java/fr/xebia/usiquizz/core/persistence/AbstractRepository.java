package fr.xebia.usiquizz.core.persistence;

import com.mongodb.DB;
import com.mongodb.Mongo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.ResourceBundle;

public abstract class AbstractRepository {

    private static Logger logger = LoggerFactory.getLogger(AbstractRepository.class);

    // Mongodb manage his pool, need only one instance
    private static Mongo mongo;
    private static DB db;
    private boolean initialized = false;

    public AbstractRepository() {
        if (!initialized) {
            try {
                ResourceBundle rb = ResourceBundle.getBundle("configuration");
                String host = rb.getString("mongodb.host");
                int port = Integer.parseInt(rb.getString("mongodb.port"));
                String mongoDb = rb.getString("mongodb.db");
                logger.info("Initialize mongo on : " + host + ":" + port);
                mongo = new Mongo(host, port);
                logger.info("Use db : " + mongoDb);
                db = mongo.getDB(mongoDb);
                initialized = true;
            }
            catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }

    public DB getDb() {
        return db;
    }


}
