package com.sparrowx.internal;

import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {

    /*
     * Intsvc security shell.
     *
     * Runtime access is enforced at gRPC boundary by:
     * - GrpcTenantContextInterceptor
     * - GrpcPolicyEnforcementInterceptor
     * - InternalAccessPolicy
     *
     * Keycloak/JWT wiring can be added here later if intsvc exposes HTTP endpoints
     * or if authentication is moved from gateway/interceptor into Spring Security.
     */
}