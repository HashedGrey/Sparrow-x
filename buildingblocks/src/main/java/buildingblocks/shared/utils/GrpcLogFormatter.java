package buildingblocks.shared.utils;

import io.grpc.Metadata;
import io.grpc.Status;

public class GrpcLogFormatter {

    public static String formatMethod(String method) {
        return "[gRPC] Method=" + method;
    }

    public static String formatStatus(Status status) {
        return "[gRPC] Status=" + status.getCode() + " Desc=" + status.getDescription();
    }

    public static String formatHeaders(Metadata headers) {
        return "[gRPC] Headers=" + headers;
    }
}
