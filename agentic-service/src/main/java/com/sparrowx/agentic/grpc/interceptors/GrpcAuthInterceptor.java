package com.sparrowx.agentic.grpc.interceptors;

import com.sparrowx.agentic.config.SecurityConfig.CallerIdentity;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;


public final class GrpcAuthInterceptor
        implements ServerInterceptor {

    public static final Context.Key<CallerIdentity> CALLER_IDENTITY =
            Context.key("sparrowx-agentic-caller-identity");

    public static final Metadata.Key<String> SUBJECT_HEADER =
            Metadata.Key.of(
                    "x-sparrowx-subject",
                    Metadata.ASCII_STRING_MARSHALLER
            );

    public static final Metadata.Key<String> TENANT_IDS_HEADER =
            Metadata.Key.of(
                    "x-sparrowx-tenant-ids",
                    Metadata.ASCII_STRING_MARSHALLER
            );

    public static final Metadata.Key<String> ROLES_HEADER =
            Metadata.Key.of(
                    "x-sparrowx-roles",
                    Metadata.ASCII_STRING_MARSHALLER
            );

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next
    ) {
        String subject = normalize(headers.get(SUBJECT_HEADER));

        if (subject.isEmpty()) {
            call.close(
                    Status.UNAUTHENTICATED
                            .withDescription(
                                    "Authenticated caller subject is required"
                            ),
                    new Metadata()
            );
            return new ServerCall.Listener<>() {
            };
        }

        CallerIdentity identity;

        try {
            identity = new CallerIdentity(
                    subject,
                    split(headers.get(TENANT_IDS_HEADER), false),
                    split(headers.get(ROLES_HEADER), true)
            );
        } catch (IllegalArgumentException exception) {
            call.close(
                    Status.UNAUTHENTICATED
                            .withDescription(exception.getMessage())
                            .withCause(exception),
                    new Metadata()
            );
            return new ServerCall.Listener<>() {
            };
        }

        Context context = Context.current()
                .withValue(CALLER_IDENTITY, identity);

        return Contexts.interceptCall(
                context,
                call,
                headers,
                next
        );
    }

    public static CallerIdentity currentIdentity() {
        CallerIdentity identity = CALLER_IDENTITY.get();

        if (identity == null) {
            throw new SecurityException(
                    "Authenticated caller identity is unavailable"
            );
        }

        return identity;
    }

    private static Set<String> split(
            String value,
            boolean uppercase
    ) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }

        Set<String> values = Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .map(item -> uppercase
                        ? item.toUpperCase(Locale.ROOT)
                        : item)
                .collect(Collectors.toCollection(
                        LinkedHashSet::new
                ));

        return Set.copyOf(values);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}