package com.alivex.mongo.repl;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractMongoReplicaConfig implements Serializable {

    public static String HOST = "localhost";
    public static int PRIMARY_PORT = 28017;
    public static int SECONDARY_PORT = 28018;
    public static String REPLICA_NAME = "rs0";

    public static MongoClientSettings getClientSetting() {
        return MongoClientSettings.builder()
                    .applyToClusterSettings(builder -> builder.hosts(getServers()))
                    .build();
    }

    public static List<ServerAddress> getServers() {
        return Arrays.asList(AbstractMongoReplicaConfig.getSeeds());
    }

    private static ServerAddress[] getSeeds() {
        return new ServerAddress[]{
            new ServerAddress(HOST, PRIMARY_PORT),
            new ServerAddress(HOST, SECONDARY_PORT)
        };
    }
}
