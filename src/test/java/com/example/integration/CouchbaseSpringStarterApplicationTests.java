package com.example.integration;

import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.kv.GetResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.Assert.assertNotNull;

/**
 * This test will run against an existing Couchbase instance configured with environment variables.
 */
@SpringBootTest

class CouchbaseSpringStarterApplicationTests {

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
