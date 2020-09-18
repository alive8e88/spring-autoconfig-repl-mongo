# How to use
```java
<dependency>
    <groupId>com.alivex.spring.config</groupId>
    <artifactId>replica-embedded-mongo</artifactId>
    <version>3.0.3</version>
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
         factory.setMongoClientSettings(AbstractMongoReplicaConfig.getClientSetting());
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


Spring is embracing a new feature shipped with MongoDB 4.x that supports multi-document transactions. That feature works only for existing collections. 
A multi-document transaction cannot include an insert operation that would result in the creation of a new collection. 
You should create your collections before hand to use this feature. 

To workaround for this issue you can explicit config spring data mongo to auto index creation that results in collection creation.

```properties
#Add this config to your test application.properties file
spring.data.mongodb.auto-index-creation=true
```

Or you can use an event listener method to prepare a collection before TransactionalTestExecutionListener begin a transaction for your test case.
```java
/**
 * This method will be invoke by TransactionalTestExecutionListener 
 * @BeforeTransaction or @AfterTransaction are not run for test methods that are not configured to run within a transaction
 * https://docs.spring.io/spring/docs/current/spring-framework-reference/testing.html#testcontext-tx-before-and-after-tx
 */
@BeforeTransaction
public void initCollection() {
    //... init mongodb collection
}

```
But if you need more control you can implement your own test exceution listener. The example below use custom TestExecutionListener to create a collection by scanning a classpath to find all class annotated with @Document 
and get collection name from them to create a new collection before begin a transaction.
```java
@TestExecutionListeners(value = {MongodbCustomTestExecutionListener.class}, mergeMode = MergeMode.MERGE_WITH_DEFAULTS)
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

public class MongodbCustomTestExecutionListener implements TestExecutionListener, Ordered  {

    private static final Logger logger = LoggerFactory.getLogger(MongodbCustomTestExecutionListener.class);

    public void prepareTestInstance(TestContext testContext) throws Exception {
        MongoClient mongoClient = testContext.getApplicationContext().getBean(MongoClient.class);
        if(mongoClient == null) return;

        MongoProperties properties = testContext.getApplicationContext().getBean(MongoProperties.class);
        logger.trace("Detect mongo properties:{}", properties);
        if(properties == null) return;

        List<String> collections = new MongodbDocumentClassPathScanner("com.your.package.require.to.scan").getCollectionName();
        if(collections.isEmpty()) return;

        logger.info("Detect default database name:{}", properties.getDatabase());
        MongoDatabase db = mongoClient.getDatabase(properties.getDatabase());
        createCollectionIfNotExist(db, collections);
    };

    private void createCollectionIfNotExist(MongoDatabase db, List<String> collections) {
        final Set<String> existingCollections = getExistingCollection(db);
        collections.stream()
                .filter(name -> !existingCollections.contains(name))
                .forEach(name -> db.createCollection(name));
    }

    private Set<String> getExistingCollection(MongoDatabase db) {
        Set<String> existingCollections = new HashSet<>();
        for (String existingCollection : db.listCollectionNames()) {
            existingCollections.add(existingCollection);
        }
        return existingCollections;
    }

    /**
     * We need to run before TransactionalTestExecutionListener(order = 4000), but after DirtiesContextTestExecutionListener(order = 3000)
     * @return
     */
    @Override
    public int getOrder() {
        return 3500;
    };
    
    public void beforeTestClass(TestContext testContext) throws Exception {
        logger.info("beforeTestClass : {}", testContext.getTestClass());
    };

    public void beforeTestMethod(TestContext testContext) throws Exception {
        logger.info("beforeTestMethod : {}", testContext.getTestClass());
    };

    public void afterTestMethod(TestContext testContext) throws Exception {
        logger.info("afterTestMethod : {}", testContext.getTestMethod());
    };

    public void afterTestClass(TestContext testContext) throws Exception {
        logger.info("afterTestClass : {}", testContext.getTestClass());
    }
}

public class MongodbDocumentClassPathScanner {

    private final String packageName;

    public MongodbDocumentClassPathScanner(String packageName) {
        this.packageName = packageName;
    }

    public List<String> getCollectionName() {

        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Document.class));

        List<String> names = new ArrayList<>();
        for (BeanDefinition beanDefinition : scanner.findCandidateComponents(packageName)) {

            if (beanDefinition instanceof AnnotatedBeanDefinition) {
                AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) beanDefinition;
                AnnotationMetadata metadata = annotatedBeanDefinition.getMetadata();
                Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(Document.class.getName());
                if(annotationAttributes != null) {
                    names.add(annotationAttributes.get("collection").toString());
                }
            }
        }

        return names;
    }
}

```
