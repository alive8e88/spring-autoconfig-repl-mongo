package com.alivex.autoconfig;

import com.alivex.mongo.repl.MongodConfigOption;
import com.alivex.mongo.repl.ReplicaEmbeddedMongo;
import com.alivex.mongo.repl.ReplicaEmbeddedMongoImpl;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class ReplicaEmbeddedMongoConfigurationTest {

    private AnnotationConfigApplicationContext context;

    @AfterEach
    public void tearDown() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    public void defaultNativeConnectionFactory() {

        load(EmptyConfiguration.class);
        ReplicaEmbeddedMongo replicaEmbededMongo = this.context.getBean(ReplicaEmbeddedMongo.class);
        assertFalse(replicaEmbededMongo.getMongodConfigOption().isEnableProcessOutput());
    }

    @Test
    public void customizerIsApplied() {
        load(CustomConfiguration.class);
        ReplicaEmbeddedMongo replicaEmbededMongo = this.context.getBean(ReplicaEmbeddedMongo.class);
        assertTrue(replicaEmbededMongo.getMongodConfigOption().isEnableProcessOutput());
    }

    private void load(Class<?> config, String... environment) {

        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applyEnvironment(applicationContext, environment);

        applicationContext.register(ReplicaEmbeddedMongoConfiguration.class);
        applicationContext.register(config);
        applicationContext.refresh();
        
        this.context = applicationContext;
    }

    private void applyEnvironment(AnnotationConfigApplicationContext context, String[] environment) {

        if (environment != null && environment.length > 0) {
            TestPropertyValues.of(environment).applyTo(context);
        }
    }

    @Configuration
    static class EmptyConfiguration {
    }

    @Configuration
    static class CustomConfiguration {

//        @Bean
//        @Primary
//        @DependsOn({"replicaEmbeddedMongo"})
//        MongoClientFactoryBean createMongoClientFactoryBean() {
//
//            MongoClientFactoryBean factory = new MongoClientFactoryBean();
//            factory.setReplicaSetSeeds(getSeeds());
//            return factory;
//        }

        @Bean(name = "replicaEmbeddedMongo", initMethod = "start", destroyMethod = "stop")
        public ReplicaEmbeddedMongo createReplicaEmbeddedMongo() {
            return new ReplicaEmbeddedMongoImpl(new MongodConfigOption(true));
        }

//        @Primary
//        @Bean(initMethod = "start", destroyMethod = "stop")
//        @DependsOn({"replicaEmbeddedMongo"})
//        @ConditionalOnMissingBean
//        public MongodExecutable embeddedMongoServer() throws IOException {
//            return null;
//        }

//        private ServerAddress[] getSeeds() {
//            return AbstractMongoReplicaConfig.getSeeds();
//        }
    }
}
