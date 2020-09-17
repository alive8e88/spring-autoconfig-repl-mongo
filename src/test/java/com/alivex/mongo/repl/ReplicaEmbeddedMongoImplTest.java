
package com.alivex.mongo.repl;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReplicaEmbeddedMongoImplTest {

    final Logger logger = LoggerFactory.getLogger(ReplicaEmbeddedMongoImplTest.class);

    @Test
    public void testDefaultConfig() {
        ReplicaEmbeddedMongoImpl impl = new ReplicaEmbeddedMongoImpl();
        assertFalse(impl.getMongodConfigOption().isEnableProcessOutput());
    }

    @Test
    public void testCustomizeConfig() {
        ReplicaEmbeddedMongoImpl impl = new ReplicaEmbeddedMongoImpl(new MongodConfigOption(true));
        assertTrue(impl.getMongodConfigOption().isEnableProcessOutput());
    }

    @Test
    public void testReadWriteToReplicaSet() {
        ReplicaEmbeddedMongoImpl instance = new ReplicaEmbeddedMongoImpl();
        MongoClient mongo = null;
        try {

            instance.start();
            mongo = createMongoClient();

            MongoDatabase funDb = mongo.getDatabase("sample");
            MongoCollection<Document> testCollection = funDb.getCollection("test");

            logger.debug(">>>>>>>> inserting data");
            testCollection.insertOne(new Document("fancy", "value"));

            logger.debug(">>>>>>>> finding data");
            assertEquals("value", testCollection.find().first().get("fancy"));

        } finally {

            if (mongo != null) {
                mongo.close();
            }

            instance.stop();
        }
    }

    /**
     * https://mongodb.github.io/mongo-java-driver/4.0/driver/tutorials/connect-to-mongodb/
     * @return
     */
    private MongoClient createMongoClient() {
        return MongoClients.create(AbstractMongoReplicaConfig.getClientSetting());
    }
}
