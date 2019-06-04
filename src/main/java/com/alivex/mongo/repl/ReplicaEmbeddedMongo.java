
package com.alivex.mongo.repl;

import com.mongodb.ReplicaSetStatus;

/**
 * Provide convenient way to start and stop multiple instance of MongodExecutable from embedded mongo library
 * @author development
 */
public interface ReplicaEmbeddedMongo {

    public void start();
    public void stop();
    public ReplicaSetStatus getStatus();
    public MongodConfigOption getMongodConfigOption();
}
