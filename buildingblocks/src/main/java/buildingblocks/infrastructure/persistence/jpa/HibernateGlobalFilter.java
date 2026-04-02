package buildingblocks.infrastructure.persistence.jpa;

import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
public class HibernateGlobalFilter {

    @PersistenceContext
    private EntityManager entityManager;

    public void enableSoftDeleteFilter() {

        Session session = entityManager.unwrap(Session.class);

        Filter filter = session.enableFilter("softDeleteFilter");
        filter.setParameter("deleted", false);
    }

    public void disableSoftDeleteFilter() {
        Session session = entityManager.unwrap(Session.class);
        session.disableFilter("softDeleteFilter");
    }
}