
package com.alivex.mongo.repl;

/**
 * Provide convenient way to start and stop multiple instance of MongodExecutable from embedded mongo library
 * @author development
 */
public interface ReplicaEmbeddedMongo {

    public void start();
    public void stop();
    public MongodConfigOption getMongodConfigOption();
}
