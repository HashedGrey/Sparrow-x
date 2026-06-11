package buildingblocks.infrastructure.grpc.interceptors;

import com.google.protobuf.Message;
import com.google.protobuf.Descriptors;
import io.grpc.*;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class GrpcRequestContextMdcInterceptor implements ServerInterceptor {

    private static final String REQUEST_ID = "request_id";
    private static final String BUSINESS_TRACE_ID = "business_trace_id";
    private static final String TENANT_ID = "tenant_id";
    private static final String USER_ID = "user_id";
    private static final String PROJECT_ID = "project_id";
    private static final String TEAM_ID = "team_id";
    private static final String CALLER_SERVICE = "caller_service";

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next
    ) {
        ServerCall.Listener<ReqT> delegate = next.startCall(call, headers);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(delegate) {

            @Override
            public void onMessage(ReqT message) {
                putRequestContextInMdc(message);
                super.onMessage(message);
            }

            @Override
            public void onHalfClose() {
                super.onHalfClose();
            }

            @Override
            public void onCancel() {
                try {
                    super.onCancel();
                } finally {
                    clearBusinessMdc();
                }
            }

            @Override
            public void onComplete() {
                try {
                    super.onComplete();
                } finally {
                    clearBusinessMdc();
                }
            }
        };
    }

    private void putRequestContextInMdc(Object message) {
        if (!(message instanceof Message protobufMessage)) {
            return;
        }

        Descriptors.FieldDescriptor contextField =
                protobufMessage.getDescriptorForType().findFieldByName("context");

        if (contextField == null || !protobufMessage.hasField(contextField)) {
            return;
        }

        Object contextObject = protobufMessage.getField(contextField);

        if (!(contextObject instanceof Message contextMessage)) {
            return;
        }

        put(contextMessage, "request_id", REQUEST_ID);
        put(contextMessage, "trace_id", BUSINESS_TRACE_ID);
        put(contextMessage, "tenant_id", TENANT_ID);
        put(contextMessage, "user_id", USER_ID);
        put(contextMessage, "project_id", PROJECT_ID);
        put(contextMessage, "team_id", TEAM_ID);
        put(contextMessage, "caller_service", CALLER_SERVICE);
    }

    private void put(Message message, String protoFieldName, String mdcKey) {
        Descriptors.FieldDescriptor field =
                message.getDescriptorForType().findFieldByName(protoFieldName);

        if (field == null) {
            return;
        }

        Object value = message.getField(field);

        if (value != null && !value.toString().isBlank()) {
            MDC.put(mdcKey, value.toString());
        }
    }

    private void clearBusinessMdc() {
        MDC.remove(REQUEST_ID);
        MDC.remove(BUSINESS_TRACE_ID);
        MDC.remove(TENANT_ID);
        MDC.remove(USER_ID);
        MDC.remove(PROJECT_ID);
        MDC.remove(TEAM_ID);
        MDC.remove(CALLER_SERVICE);
    }
}