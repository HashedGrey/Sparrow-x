package buildingblocks.infrastructure.persistence.jpa;

import buildingblocks.shared.context.AuthContext;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

public class JpaAuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.ofNullable(AuthContext.getCurrentUserId());
    }
}