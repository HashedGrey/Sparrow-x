package buildingblocks.core.queries;

import buildingblocks.core.queries.interceptors.QueryInterceptor;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultQueryBus implements QueryBus {

    private final ApplicationContext context;
    private final List<QueryInterceptor> interceptors;
    private final ConcurrentHashMap<Class<?>, QueryHandler<?, ?>> handlerCache = new ConcurrentHashMap<>();

    public DefaultQueryBus(List<QueryInterceptor> interceptors, ApplicationContext context) {
        this.interceptors = interceptors;
        this.context = context;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Q extends IQuery<R>, R> R send(Q query) {

        QueryHandler<Q, R> chain = resolveHandler(query);
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            QueryInterceptor interceptor = interceptors.get(i);
            QueryHandler<Q, R> next = chain;
            chain = q -> interceptor.intercept(q, next);
        }

        return chain.handle(query);
    }

    @SuppressWarnings("unchecked")
    private <Q extends IQuery<R>, R> QueryHandler<Q, R> resolveHandler(Q query) {
        return (QueryHandler<Q, R>) handlerCache.computeIfAbsent(
                query.getClass(),
                clazz -> context.getBean(clazz.getSimpleName() + "Handler", QueryHandler.class)
        );
    }
}
