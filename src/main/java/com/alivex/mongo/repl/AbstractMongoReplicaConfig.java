package com.alivex.mongo.repl;

import com.mongodb.ServerAddress;
import java.io.Serializable;

public abstract class AbstractMongoReplicaConfig implements Serializable {

    public static String HOST = "localhost";
    public static int PRIMARY_PORT = 27017;
    public static int SECONDARY_PORT = 27018;
    public static String REPLICA_NAME = "rs0";

    public static ServerAddress[] getSeeds() {

        return new ServerAddress[]{
            new ServerAddress(HOST, PRIMARY_PORT),
            new ServerAddress(HOST, SECONDARY_PORT)
        };
    }
}
