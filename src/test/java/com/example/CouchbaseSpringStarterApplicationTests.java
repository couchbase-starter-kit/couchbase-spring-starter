package com.example;

import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import org.junit.jupiter.api.AfterAll;
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

import java.time.Duration;

import static com.example.CouchbaseContainerMetadata.COUCHBASE_IMAGE_ENTERPRISE;
import static com.example.CouchbaseContainerMetadata.bucketDefinition;


@SpringBootTest
@Testcontainers
class CouchbaseSpringStarterApplicationTests {
    //Define the couchbase container.
    @Container
    final static CouchbaseContainer couchbaseContainer = new CouchbaseContainer(COUCHBASE_IMAGE_ENTERPRISE)
            .withCredentials(CouchbaseContainerMetadata.USERNAME, CouchbaseContainerMetadata.PASSWORD)
            .withEnabledServices(CouchbaseService.KV, CouchbaseService.QUERY, CouchbaseService.INDEX, CouchbaseService.SEARCH)
            .withBucket(bucketDefinition)
            .withStartupAttempts(4)
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
    }

    @Autowired
    Scope scope;
    @Autowired
    Collection collection;

    @Test
    void contextLoads() throws Exception{
        scope.query("Select * from system:indexes");
        collection.upsert("key", "content");
        collection.get("key");
        // sleep to make sure OTEL metrics are sent
		Thread.sleep(10000);
    }

}
