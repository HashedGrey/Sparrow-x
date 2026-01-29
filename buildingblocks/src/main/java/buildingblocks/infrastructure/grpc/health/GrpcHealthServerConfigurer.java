package buildingblocks.infrastructure.grpc.health;

import io.grpc.ServerServiceDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcHealthServerConfigurer {
    private final GrpcHealthAdapter healthAdapter;

    public GrpcHealthServerConfigurer(GrpcHealthAdapter healthAdapter) {
        this.healthAdapter = healthAdapter;
    }

    @Bean
    public ServerServiceDefinition grpcHealthServiceDefinition() {
        return healthAdapter.getHealthService();
    }
}