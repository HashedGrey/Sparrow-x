package buildingblocks.infrastructure.observability;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class MetricRegistryUtil {

    private static final MeterRegistry registry = new SimpleMeterRegistry();

    public static MeterRegistry registry() {
        return registry;
    }

    public static Counter counter(String name, String... tags) {
        return Counter.builder(name).tags(tags).register(registry);
    }

    public static Timer timer(String name, String... tags) {
        return Timer.builder(name).tags(tags).register(registry);
    }

    public static Gauge gauge(String name, Number number, String... tags) {
        return Gauge.builder(name, number::doubleValue).tags(tags).register(registry);
    }
}
