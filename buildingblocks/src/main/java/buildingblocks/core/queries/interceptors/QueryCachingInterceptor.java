package buildingblocks.core.queries.interceptors;

import buildingblocks.core.queries.Query;
import buildingblocks.core.queries.QueryExecutionChain;
import buildingblocks.core.queries.QueryInterceptor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class QueryCachingInterceptor implements QueryInterceptor {

    @Override
    public <R> R intercept(Query<R> query, QueryExecutionChain<R> chain) {
        // check cache key by query type + serialized args
        // return cached result if hit
        // otherwise proceed and cache result
        return chain.proceed(query);
    }
}