package buildingblocks.infrastructure.grpc.health;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Expose simple /live and /ready endpoints so K8s can probe via HTTP.
 * The internal state maps to the gRPC health global status.
 */
@RestController
public class HttpLivenessController {

    private final GrpcHealthAdapter healthAdapter;

    public HttpLivenessController(GrpcHealthAdapter healthAdapter) {
        this.healthAdapter = healthAdapter;
    }


    @GetMapping("/live")
    public ResponseEntity<String> live() {
// Liveness: JVM / process-level. If app is running, return 200.
        return ResponseEntity.ok("OK");
    }


    @GetMapping("/ready")
    public ResponseEntity<String> ready() {
        switch (healthAdapter.getGlobalStatus()) {
            case SERVING:
                return ResponseEntity.ok("SERVING");
            case NOT_SERVING:
            case UNKNOWN:
            default:
                return ResponseEntity.status(503).body("NOT_SERVING");
        }
    }
}