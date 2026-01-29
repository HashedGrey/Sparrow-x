package buildingblocks.core.queries.interceptors;

import buildingblocks.core.queries.IQuery;
import buildingblocks.core.queries.QueryHandler;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class QueryAuthInterceptor implements QueryInterceptor {

    @Override
    public <Q extends IQuery<R>, R> R intercept(Q query, QueryHandler<Q, R> next) {
        // auth / scope checks
        return next.handle(query);
    }
}
