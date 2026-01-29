package buildingblocks.infrastructure.observability;

import io.grpc.Metadata;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;

public class GrpcTracingPropagator {

    private static final TextMapGetter<Metadata> METADATA_GETTER = new TextMapGetter<>() {
        @Override
        public Iterable<String> keys(Metadata carrier) {
            return carrier.keys();
        }

        @Override
        public String get(Metadata carrier, String key) {
            if (carrier == null) {
                return null;
            }
            Metadata.Key<String> headerKey = Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER);
            return carrier.get(headerKey);
        }
    };

    public static Context extractContext(Metadata headers) {
        return GlobalOpenTelemetry.getPropagators()
                .getTextMapPropagator()
                .extract(Context.current(), headers, METADATA_GETTER);
    }
}
