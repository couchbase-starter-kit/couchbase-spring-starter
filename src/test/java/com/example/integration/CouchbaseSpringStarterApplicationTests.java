package com.example.integration;

import java.time.Duration;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.kv.GetResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This test will run against an existing Couchbase instance configured with environment variables.
 */
@SpringBootTest

class CouchbaseSpringStarterApplicationTests {

    @Autowired
    Cluster cluster;
    @Autowired
    Scope scope;
    @Autowired
    Collection collection;

    @Test
    void contextLoads() throws Exception{
        cluster.waitUntilReady(Duration.ofMillis(1000));
        scope.query("Select * from system:indexes");
        collection.upsert("key", "content");
        GetResult res = collection.get("key");
        Assertions.assertNotNull(res.contentAsBytes());
    }

}
