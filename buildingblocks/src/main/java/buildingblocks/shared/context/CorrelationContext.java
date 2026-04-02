package buildingblocks.shared.context;

public final class CorrelationContext {

    private static final ThreadLocal<String> correlationId = new ThreadLocal<>();
    private static final ThreadLocal<String> traceId = new ThreadLocal<>();

    private CorrelationContext() {}

    public static void setCorrelationId(String id) {
        correlationId.set(id);
    }

    public static String getCorrelationId() {
        return correlationId.get();
    }

    public static void setTraceId(String id) {
        traceId.set(id);
    }

    public static String getTraceId() {
        return traceId.get();
    }

    public static void clear() {
        correlationId.remove();
        traceId.remove();
    }
}