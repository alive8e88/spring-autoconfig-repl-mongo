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
public class DemoApplicationTest {

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

Spring caches the application context by default when running tests. And sometime mongod process cannot stop gracefully after test excution. 
For this reasons if you need to cleanup application context, use @DirtiesContext. @DirtiesContext is an extremely expensive resource when it comes to execution time, 
and as such, we should be careful. 
```java
@DirtiesContext
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = NONE)
@EnableAutoConfiguration(exclude = {EmbeddedMongoAutoConfiguration.class, ReplicaEmbeddedMongoConfiguration.class})
public class DemoApplicationTest {

 @Autowired
 private MongoClient mongo;

 @Test
 public void test() {
     // test code
 }
 
}
```