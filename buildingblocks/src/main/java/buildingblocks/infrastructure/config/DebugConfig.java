package buildingblocks.infrastructure.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class DebugConfig {

    @Value("${grpc.debug.enabled:false}")
    private boolean grpcDebugEnabled;

}
