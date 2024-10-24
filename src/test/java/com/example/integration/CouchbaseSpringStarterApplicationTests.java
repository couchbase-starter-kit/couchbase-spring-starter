package com.example.integration;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.couchbase.client.core.Core;
import com.couchbase.client.core.deps.io.netty.util.concurrent.CompleteFuture;
import com.couchbase.client.core.io.CollectionIdentifier;
import com.couchbase.client.core.transaction.components.DocumentGetter;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.kv.GetOptions;
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
        Core core = cluster.core();

        CollectionIdentifier ci = new CollectionIdentifier("default", Optional.of("_default"),  Optional.of("_default"));
        CustomGetRequest req = new CustomGetRequest("key", core.environment().timeoutConfig().kvTimeout(), core.context(), ci, core.environment().retryStrategy(), null);
        core.send(req);
        CompletableFuture<CustomGetResponse> future = req.response();
        CustomGetResponse response = future.get();
        
        Assertions.assertEquals(response.key(), "key");

        GetResult res = collection.get("key", GetOptions.getOptions());
        
        Assertions.assertNotNull(res.contentAsBytes());
    }

}
