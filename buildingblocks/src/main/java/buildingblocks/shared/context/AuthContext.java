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
    private String userId;
    private String tenantId;

    @Setter(AccessLevel.NONE) // roles can’t be set directly; use addRole/removeRole
    private final Set<String> roles = new HashSet<>();

    // Add a role
    public void addRole(String role) {
        if (role != null) roles.add(role);
    }

    // Remove a role
    public void removeRole(String role) {
        roles.remove(role);
    }

    // Check if user has a role
    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    // Return unmodifiable view
    public Set<String> getRoles() {
        return Collections.unmodifiableSet(roles);
    }
}
