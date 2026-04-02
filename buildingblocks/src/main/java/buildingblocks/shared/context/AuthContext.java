package buildingblocks.shared.context;

import lombok.Getter;
import lombok.Setter;
import lombok.AccessLevel;
import lombok.experimental.SuperBuilder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@SuperBuilder
public class AuthContext {

    private static final ThreadLocal<AuthContext> CURRENT = new ThreadLocal<>();

    private String userId;
    private String tenantId;

    @Setter(AccessLevel.NONE)
    private final Set<String> roles = new HashSet<>();

    // ===== Context lifecycle =====

    public static void set(AuthContext context) {
        CURRENT.set(context);
    }

    public static AuthContext get() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }

    public static String getCurrentUserId() {
        AuthContext ctx = CURRENT.get();
        return ctx != null ? ctx.getUserId() : null;
    }

    public static String getCurrentTenantId() {
        AuthContext ctx = CURRENT.get();
        return ctx != null ? ctx.getTenantId() : null;
    }

    // ===== Roles =====

    public void addRole(String role) {
        if (role != null) roles.add(role);
    }

    public void removeRole(String role) {
        roles.remove(role);
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public Set<String> getRoles() {
        return Collections.unmodifiableSet(roles);
    }
}