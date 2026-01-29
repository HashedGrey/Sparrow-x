package buildingblocks.core.queries.interceptors;

import buildingblocks.core.queries.IQuery;
import buildingblocks.core.queries.QueryHandler;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class QueryCachingInterceptor implements QueryInterceptor {

    @Override
    public <Q extends IQuery<R>, R> R intercept(Q query, QueryHandler<Q, R> next) {
        // lookup cache
        return next.handle(query);
    }
}
