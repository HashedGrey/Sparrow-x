package buildingblocks.core.observability;

import java.util.function.Supplier;

public abstract class BaseObservabilityInterceptor {

    protected final BaseLogger logger = new BaseLogger(getClass());

    protected <T> T observe(
            String type,
            String name,
            Supplier<T> execution
    ) {
        long start = System.nanoTime();

        logger.info("{}.start {}", type, name);

        try {
            T result = execution.get();

            long durationNanos = System.nanoTime() - start;
            long durationMs = durationNanos / 1_000_000;

            logger.info("{}.complete {} in {} ms", type, name, durationMs);

            return result;

        } catch (Exception ex) {
            long durationNanos = System.nanoTime() - start;
            long durationMs = durationNanos / 1_000_000;

            logger.error("{}.failed {} after {} ms", type, name, durationMs, ex);

            throw ex;
        }
    }
}