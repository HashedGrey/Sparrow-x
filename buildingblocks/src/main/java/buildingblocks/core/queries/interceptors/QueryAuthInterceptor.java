package buildingblocks.core.queries.interceptors;

import buildingblocks.core.queries.Query;
import buildingblocks.core.queries.QueryExecutionChain;
import buildingblocks.core.queries.QueryInterceptor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class QueryAuthInterceptor implements QueryInterceptor {

    @Override
    public <R> R intercept(Query<R> query, QueryExecutionChain<R> chain) {
        // auth / scope / tenant checks
        return chain.proceed(query);
    }
}