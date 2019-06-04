
package com.alivex.mongo.repl;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.Arrays;
import java.util.List;
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

//            System.out.println(">>>>>>>> inserting data");
            testCollection.insertOne(new Document("fancy", "value"));

//            System.out.println(">>>>>>>> finding data");
            assertEquals("value", testCollection.find().first().get("fancy"));

        } finally {

            if (mongo != null) {
                mongo.close();
            }
            
            instance.stop();
        }
    }

    private MongoClient createMongoClient() {
        return new MongoClient(getSeeds());
    }

    private List<ServerAddress> getSeeds() {
        return Arrays.asList(AbstractMongoReplicaConfig.getSeeds());
    }
}
