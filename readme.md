# How to use
```java
<dependency>
    <groupId>com.alivex.spring.config</groupId>
    <artifactId>replica-embedded-mongo</artifactId>
    <version>0.0.1</version>
    <scope>test</scope>
</dependency>
```

# Customize
```java
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = NONE)
@EnableAutoConfiguration(exclude = {EmbeddedMongoAutoConfiguration.class, ReplicaEmbeddedMongoConfiguration.class})
public class DemoApplicationTestV4 {

 @Autowired
 private MongoClient mongo;

 @Test
 public void test() {
     assertNotNull(mongo.getReplicaSetStatus());
 }

 @TestConfiguration
 static class CustomConfig {

     @Bean
     @Primary
     @DependsOn({"replicaEmbeddedMongo"})
     MongoClientFactoryBean mongoClientFactoryBean() {

         MongoClientFactoryBean factory = new MongoClientFactoryBean();
         factory.setReplicaSetSeeds(AbstractMongoReplicaConfig.getSeeds());
         return factory;
     }

     @Bean(name = "replicaEmbeddedMongo", initMethod = "start", destroyMethod = "stop")
     public ReplicaEmbeddedMongo replicaEmbeddedMongo() {
         //enable mongod process output
         return new ReplicaEmbeddedMongoImpl(new MongodConfigOption(true));
     }
  }    
}
```