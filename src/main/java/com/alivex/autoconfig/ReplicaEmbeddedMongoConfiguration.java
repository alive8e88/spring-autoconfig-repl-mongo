package com.alivex.autoconfig;

import com.alivex.mongo.repl.AbstractMongoReplicaConfig;
import com.alivex.mongo.repl.ReplicaEmbeddedMongo;
import com.alivex.mongo.repl.ReplicaEmbeddedMongoImpl;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import java.io.IOException;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.Ordered;
import org.springframework.data.mongodb.core.MongoClientFactoryBean;

@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@Configuration
@ConditionalOnClass({MongoClient.class, MongodStarter.class})
public class ReplicaEmbeddedMongoConfiguration {

    @Bean
//    @Primary
    @DependsOn({"replicaEmbeddedMongo"})
    public MongoClientFactoryBean createMongoClientFactoryBean() {

        MongoClientFactoryBean factory = new MongoClientFactoryBean();
        factory.setReplicaSetSeeds(getSeeds());
        return factory;
    }

    @Bean(name = "replicaEmbeddedMongo", initMethod = "start", destroyMethod = "stop")
    public ReplicaEmbeddedMongo createReplicaEmbeddedMongo() {
        return new ReplicaEmbeddedMongoImpl();
    }

//    @Primary
    @Bean(initMethod = "start", destroyMethod = "stop")
    @DependsOn({"replicaEmbeddedMongo"})
    @ConditionalOnMissingBean
    public MongodExecutable embeddedMongoServer() throws IOException {
        return null;
    }

    private ServerAddress[] getSeeds() {
        return AbstractMongoReplicaConfig.getSeeds();
    }
}
