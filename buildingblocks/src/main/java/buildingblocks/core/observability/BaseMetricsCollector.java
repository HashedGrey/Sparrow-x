package buildingblocks.core.observability;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class BaseMetricsCollector {

    private final MeterRegistry meterRegistry;

    public BaseMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void increment(String metricName) {
        meterRegistry.counter(metricName).increment();
    }

    public void increment(String metricName, String... tags) {
        meterRegistry.counter(metricName, tags).increment();
    }

    public void recordTime(String metricName, long nanos) {
        meterRegistry.timer(metricName).record(nanos, TimeUnit.NANOSECONDS);
    }

    public void recordTime(String metricName, long nanos, String... tags) {
        meterRegistry.timer(metricName, tags).record(nanos, TimeUnit.NANOSECONDS);
    }
}