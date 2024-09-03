package com.example.testcontainers;

import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.kv.GetResult;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.couchbase.CouchbaseContainer;
import org.testcontainers.couchbase.CouchbaseService;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.example.testcontainers.CouchbaseContainerMetadata.COUCHBASE_IMAGE_ENTERPRISE;
import static com.example.testcontainers.CouchbaseContainerMetadata.bucketDefinition;

import java.time.Duration;

/**
 * This is test will run against a Couchbase instance managed by testcontainers.
 */
@SpringBootTest
@Testcontainers
class CouchbaseSpringStarterApplicationTests {
    //Define the couchbase container.
    @Container
    final static CouchbaseContainer couchbaseContainer = new CouchbaseContainer(COUCHBASE_IMAGE_ENTERPRISE)
            .withCredentials(CouchbaseContainerMetadata.USERNAME, CouchbaseContainerMetadata.PASSWORD)
            .withEnabledServices(CouchbaseService.KV, CouchbaseService.QUERY, CouchbaseService.INDEX, CouchbaseService.SEARCH)
            .withBucket(bucketDefinition)
            .withStartupAttempts(10)
            .withStartupTimeout(Duration.ofSeconds(90))
            .waitingFor(Wait.forHealthcheck());

    @AfterAll
    public static void teardown() {
        couchbaseContainer.stop();
    }

    @DynamicPropertySource
    static void bindCouchbaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.couchbase.connection-string", couchbaseContainer::getConnectionString);
        registry.add("spring.couchbase.username", couchbaseContainer::getUsername);
        registry.add("spring.couchbase.password", couchbaseContainer::getPassword);
        registry.add("couchbase.useCapella", () -> false);
        registry.add("couchbase.defaultBucket", () -> "default");
        registry.add("couchbase.defaultScope", () -> "_default");
        registry.add("couchbase.defaultCollection", () -> "_default");
    }

    @Autowired
    Scope scope;
    @Autowired
    Collection collection;

    @Test
    void contextLoads() throws Exception{
        scope.query("Select * from system:indexes");
        collection.upsert("key", "content");
        GetResult res = collection.get("key");
        Assertions.assertNotNull(res.contentAsBytes());
    }

}
