package com.sparrowx.internal.grpc.interceptors;

import com.sparrowx.internal.grpc.policies.InternalAccessPolicy;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.util.Arrays;


public class GrpcPolicyEnforcementInterceptor implements ServerInterceptor {

    private static final Logger log =
            LoggerFactory.getLogger(GrpcPolicyEnforcementInterceptor.class);

    private final InternalAccessPolicy internalAccessPolicy;
    private final Environment environment;

    public GrpcPolicyEnforcementInterceptor(
            InternalAccessPolicy internalAccessPolicy,
            Environment environment
    ) {
        this.internalAccessPolicy = internalAccessPolicy;
        this.environment = environment;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next
    ) {
        var methodName = call.getMethodDescriptor().getFullMethodName();

        if (isProdProfile()) {
            internalAccessPolicy.assertAllowed(methodName, headers);
            log.debug("Intsvc access policy allowed method={}", methodName);
        } else {
            log.debug("Intsvc access policy skipped outside prod method={}", methodName);
        }

        return next.startCall(call, headers);
    }

    private boolean isProdProfile() {
        return Arrays.asList(environment.getActiveProfiles()).contains("prod");
    }
}