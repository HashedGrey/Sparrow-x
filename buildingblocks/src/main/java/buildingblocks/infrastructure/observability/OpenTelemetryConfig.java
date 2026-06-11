package buildingblocks.infrastructure.observability;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.semconv.ServiceAttributes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class OpenTelemetryConfig {

    @Bean
    public OpenTelemetry openTelemetry(
            @Value("${sparrowx.observability.otlp.endpoint:http://localhost:4317}")
            String otlpEndpoint,

            @Value("${spring.application.name:sparrowx-service}")
            String serviceName
    ) {
        Resource resource = Resource.getDefault()
                .merge(Resource.builder()
                        .put(ServiceAttributes.SERVICE_NAME, serviceName)
                        .put("service.namespace", "sparrowx")
                        .put("service.version", "1.0.0")
                        .put("deployment.environment", "dev")
                        .build());

        OtlpGrpcSpanExporter otlpExporter =
                OtlpGrpcSpanExporter.builder()
                        .setEndpoint(otlpEndpoint)
                        .setTimeout(Duration.ofSeconds(5))
                        .build();

        SdkTracerProvider tracerProvider =
                SdkTracerProvider.builder()
                        .setResource(resource)
                        .addSpanProcessor(
                                BatchSpanProcessor.builder(otlpExporter)
                                        .build()
                        )
                        .addSpanProcessor(
                                SimpleSpanProcessor.create(
                                        LoggingSpanExporter.create()
                                )
                        )
                        .build();

        return OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .setPropagators(
                        ContextPropagators.create(
                                W3CTraceContextPropagator.getInstance()
                        )
                )
                .build();
    }

    @Bean
    public Tracer tracer(
            OpenTelemetry openTelemetry,
            @Value("${spring.application.name:sparrowx-service}")
            String serviceName
    ) {
        return openTelemetry.getTracer(serviceName);
    }
}