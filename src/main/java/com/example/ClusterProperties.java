package com.example;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "couchbase")
public record ClusterProperties(OTLP  otlp, boolean useCapella, String defaultBucket, String defaultScope, String defaultCollection) {
}

record OTLP(boolean enabled, String endpoint){}