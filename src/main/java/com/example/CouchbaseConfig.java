package com.example;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.metrics.opentelemetry.OpenTelemetryMeter;
import com.couchbase.client.tracing.opentelemetry.OpenTelemetryRequestTracer;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.*;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.couchbase.ClusterEnvironmentBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
public class CouchbaseConfig  {

    @Autowired
    private ClusterProperties clusterProperties;

    @Value("${spring.application.name}")
    private String appName;

    @Bean
    public ClusterEnvironmentBuilderCustomizer clusterEnvironmentBuilderCustomizer() {
        return new ClusterEnvironmentBuilderCustomizer() {
            @Override
            public void customize(ClusterEnvironment.Builder builder) {
                if (clusterProperties.otlp().enabled()) {
                    // Setup an exporter.
                    // This exporter exports traces on the OTLP protocol over GRPC to localhost:4317.
                    MetricExporter exporter = OtlpGrpcMetricExporter.builder()
                            .setCompression("gzip")
                            .setEndpoint(clusterProperties.otlp().endpoint())
                            .build();

                    // Set the OpenTelemetry SDK's SdkTracerProvider
                    SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                            .setResource(Resource.getDefault()
                                    .merge(Resource.builder()
                                            // An OpenTelemetry service name generally reflects the name of your microservice,
                                            // e.g. "shopping-cart-service".
                                            .put("service.name", appName)
                                            .build()))
                            // The BatchSpanProcessor will efficiently batch traces and periodically export them.
                            // This exporter exports traces on the OTLP protocol over GRPC to localhost:4317.
                            .addSpanProcessor(BatchSpanProcessor.builder(OtlpGrpcSpanExporter.builder()
                                    .setEndpoint(clusterProperties.otlp().endpoint())
                                    .build()).build())
                            // Export every trace: this may be too heavy for production.
                            // An alternative is `.setSampler(Sampler.traceIdRatioBased(0.01))`
                            .setSampler(Sampler.alwaysOn())
                            .build();


                    // Create the OpenTelemetry SDK's SdkMeterProvider.
                    SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
                            .setResource(Resource.getDefault()
                                    .merge(Resource.builder()
                                            // An OpenTelemetry service name generally reflects the name of your microservice,
                                            // e.g. "shopping-cart-service".
                                            .put("service.name", appName)
                                            .build()))
                            // Operation durations are in nanoseconds, which are too large for the default OpenTelemetry histogram buckets.
                            .registerView(InstrumentSelector.builder().setType(InstrumentType.HISTOGRAM).build(),
                                    View.builder().setAggregation(Aggregation.explicitBucketHistogram(List.of(
                                            100000.0,
                                            250000.0,
                                            500000.0,
                                            1000000.0,
                                            10000000.0,
                                            100000000.0,
                                            1000000000.0,
                                            10000000000.0))).build())
                            .registerMetricReader(PeriodicMetricReader.builder(exporter).setInterval(Duration.ofSeconds(1)).build())
                            .build();

                    // Create the OpenTelemetry SDK's OpenTelemetry object.
                    OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
                            .setMeterProvider(sdkMeterProvider)
                            .setTracerProvider(sdkTracerProvider)
                            .buildAndRegisterGlobal();

                    // Provide the OpenTelemetry object as part of the Cluster configuration.
                    builder.meter(OpenTelemetryMeter.wrap(openTelemetry));
                    builder.requestTracer(OpenTelemetryRequestTracer.wrap(openTelemetry));
                }
                if (clusterProperties.useCapella()) builder.applyProfile("wan-development").securityConfig().enableTls(true);}
        };
    }

    private String getBucketName() {
        if (clusterProperties.defaultBucket() != null)
            return clusterProperties.defaultBucket();
        else return "default";
    }

    private String getScopeName() {
        if (clusterProperties.defaultScope() != null)
            return clusterProperties.defaultScope();
        else return "_default";
    }

    private String getCollectionName() {
        if (clusterProperties.defaultCollection() != null)
            return clusterProperties.defaultCollection();
        else return "_default";
    }

    @Bean
    public Bucket bucket(Cluster cluster){
        return cluster.bucket(getBucketName());
    }

    @Bean
    public Scope scope(Bucket bucket){
        return bucket.scope(getScopeName());
    }

    @Bean
    public Collection collection(Scope scope){
        return scope.collection(getCollectionName());
    }


}
