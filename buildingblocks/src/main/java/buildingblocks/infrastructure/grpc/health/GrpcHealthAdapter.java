package buildingblocks.infrastructure.grpc.health;

import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.protobuf.services.HealthStatusManager;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicReference;


@Component
public class GrpcHealthAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(GrpcHealthAdapter.class);


    private final HealthStatusManager healthStatusManager = new HealthStatusManager();
    private final AtomicReference<HealthCheckResponse.ServingStatus> globalStatus = new AtomicReference<>(HealthCheckResponse.ServingStatus.UNKNOWN);


    public GrpcHealthAdapter() {
// Default: not serving until ApplicationReadyEvent triggers.
        healthStatusManager.setStatus("", HealthCheckResponse.ServingStatus.UNKNOWN);
    }


    /**
     * Returns the underlying HealthService to register with ServerBuilder.
     */
    public io.grpc.ServerServiceDefinition getHealthService() {
        return healthStatusManager.getHealthService().bindService();
    }


    /**
     * Called when the app is ready. Marks core service as SERVING.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        LOG.info("Application ready — setting gRPC global health to SERVING");
        setServing("");
    }


    public void setServing(String serviceName) {
        globalStatus.set(HealthCheckResponse.ServingStatus.SERVING);
        healthStatusManager.setStatus(serviceName, HealthCheckResponse.ServingStatus.SERVING);
    }


    public void setNotServing(String serviceName) {
        globalStatus.set(HealthCheckResponse.ServingStatus.NOT_SERVING);
        healthStatusManager.setStatus(serviceName, HealthCheckResponse.ServingStatus.NOT_SERVING);
    }


    public HealthCheckResponse.ServingStatus getGlobalStatus() {
        return globalStatus.get();
    }


    /**
     * Mark NOT_SERVING at JVM shutdown so the health checks stop routing traffic.
     */
    @PreDestroy
    public void onShutdown() {
        LOG.info("Shutting down — setting gRPC global health to NOT_SERVING");
        setNotServing("");
    }
}