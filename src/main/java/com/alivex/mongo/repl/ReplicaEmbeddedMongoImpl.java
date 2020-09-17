
package com.alivex.mongo.repl;

import com.mongodb.BasicDBList;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongoCmdOptionsBuilder;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.exceptions.DistributionException;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.io.Slf4jLevel;
import de.flapdoodle.embed.process.runtime.Network;
import java.io.IOException;
import java.net.UnknownHostException;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReplicaEmbeddedMongoImpl extends AbstractMongoReplicaConfig implements ReplicaEmbeddedMongo {

    private final Logger LOG = LoggerFactory.getLogger(getClass().getName());

    private MongodExecutable node1MongodExe;
    private MongodProcess node1Mongod;
    private MongoClient mongo;
    private MongodExecutable node2MongodExe;
    private MongodProcess node2Mongod;
    private final MongodConfigOption mongodOptions;

    public ReplicaEmbeddedMongoImpl() {
        this.mongodOptions = new MongodConfigOption();
    }

    public ReplicaEmbeddedMongoImpl(MongodConfigOption mongodOptions) {
        this.mongodOptions = mongodOptions;
    }

    @Override
    public void start() {

        try {

            startMongoProcesses();
            initMongoClient();
            initiateReplicaSet();

        } catch (IOException e) {

            throw new IllegalStateException("Failed to start embeded mongo replica set.", e);

        } catch (DistributionException e) {
            throw new IllegalStateException("Failed to start embeded mongo replica set.", e);
        }
    }

    @Override
    public void stop() {

        try {
            if (mongo != null) {
                mongo.close();
            }
            if (node1Mongod != null) {
                node1Mongod.stop();
            }
            if (node2Mongod != null) {
                node2Mongod.stop();
            }
            if (node1MongodExe != null) {
                node1MongodExe.stop();
            }
            if (node2MongodExe != null) {
                node2MongodExe.stop();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to stop embeded mongo replica set.", e);
        }
    }

    private void startMongoProcesses() throws IOException, DistributionException {

        //Get runtime instance base on config option
        MongodStarter runtime = getMongodStarter(mongodOptions);

        node1MongodExe = runtime.prepare(new MongodConfigBuilder().version(Version.Main.V4_0)
                .withLaunchArgument("--replSet", REPLICA_NAME)
                .cmdOptions(new MongoCmdOptionsBuilder().useNoJournal(false).build())
                .net(new Net(PRIMARY_PORT, Network.localhostIsIPv6())).build());
        node1Mongod = node1MongodExe.start();

        node2MongodExe = runtime.prepare(new MongodConfigBuilder().version(Version.Main.V4_0)
                .withLaunchArgument("--replSet", REPLICA_NAME)
                .cmdOptions(new MongoCmdOptionsBuilder().useNoJournal(false).build())
                .net(new Net(SECONDARY_PORT, Network.localhostIsIPv6())).build());
        node2Mongod = node2MongodExe.start();
    }

    private MongodStarter getMongodStarter(MongodConfigOption options) {
        return options.isEnableProcessOutput() ? createMongodStarterInstance() : createMongodStarterSilientInstance();
    }

    /**
     * Without process output
     * @return
     */
    private MongodStarter createMongodStarterSilientInstance() {

        IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
                .defaultsWithLogger(Command.MongoD, LOG)
                .processOutput(ProcessOutput.getDefaultInstanceSilent())
		.build();

	return MongodStarter.getInstance(runtimeConfig);
    }

    /**
     * With process output
     * @return
     */
    private MongodStarter createMongodStarterInstance() {

        ProcessOutput processOutput = new ProcessOutput(Processors.logTo(LOG, Slf4jLevel.INFO),
                                                        Processors.logTo(LOG, Slf4jLevel.ERROR),
                                                        Processors.named("[console>]", Processors.logTo(LOG, Slf4jLevel.DEBUG)));

        IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
                .defaultsWithLogger(Command.MongoD, LOG)
                .processOutput(processOutput)
		.build();

	return MongodStarter.getInstance(runtimeConfig);
    }

    private void initMongoClient() throws UnknownHostException {
        //https://docs.mongodb.com/manual/reference/connection-string/#examples
        mongo = MongoClients.create("mongodb://" + HOST + ":" + PRIMARY_PORT);
//        mongo = new MongoClient(new ServerAddress(Network.getLocalHost(), PRIMARY_PORT));
    }

    private void initiateReplicaSet() {

        MongoDatabase adminDatabase = mongo.getDatabase("admin");
        adminDatabase.runCommand(new Document("replSetInitiate", createReplicaConfig(PRIMARY_PORT, SECONDARY_PORT)));
        LOG.debug(">>>>>>>>" + adminDatabase.runCommand(new Document("replSetGetStatus", 1)));
    }

    private Document createReplicaConfig(int node1Port, int node2Port) {

        Document config = new Document("_id", REPLICA_NAME);

        BasicDBList members = new BasicDBList();
        members.add(new Document("_id", 0).append("host", HOST + ":" + node1Port));
        members.add(new Document("_id", 1).append("host", HOST + ":" + node2Port));

        config.put("members", members);
        return config;
    }

    @Override
    public MongodConfigOption getMongodConfigOption() {
        return this.mongodOptions;
    }
}
