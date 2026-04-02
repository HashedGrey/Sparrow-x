package buildingblocks.core.queries;

import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class QueryBusImpl implements QueryBus {

    private final ApplicationContext applicationContext;
    private final List<QueryInterceptor> interceptors;

    private final ConcurrentHashMap<Class<?>, QueryHandler<?, ?>> handlerCache =
            new ConcurrentHashMap<>();

    public QueryBusImpl(
            ApplicationContext applicationContext,
            List<QueryInterceptor> interceptors
    ) {
        this.applicationContext = applicationContext;
        this.interceptors = interceptors == null ? List.of() : List.copyOf(interceptors);
    }

    @Override
    public <R> R dispatch(Query<R> query) {

        if (query == null) {
            throw new IllegalArgumentException("Query must not be null");
        }

        QueryHandler<Query<R>, R> handler = resolveHandler(query);
        QueryExecutionChain<R> chain = buildInterceptorChain(handler);

        return chain.proceed(query);
    }

    @SuppressWarnings("unchecked")
    private <R> QueryHandler<Query<R>, R> resolveHandler(Query<R> query) {

        Class<?> queryType = query.getClass();

        return (QueryHandler<Query<R>, R>) handlerCache.computeIfAbsent(
                queryType,
                type -> {
                    String[] beanNames =
                            applicationContext.getBeanNamesForType(QueryHandler.class);

                    for (String beanName : beanNames) {

                        Class<?> beanClass = applicationContext.getType(beanName);

                        if (beanClass == null) {
                            continue;
                        }

                        ResolvableType resolvableType =
                                ResolvableType.forClass(beanClass)
                                        .as(QueryHandler.class);

                        Class<?> handlerQueryType =
                                resolvableType.getGeneric(0).resolve();

                        if (handlerQueryType != null &&
                                handlerQueryType.equals(type)) {

                            return applicationContext.getBean(beanName, QueryHandler.class);
                        }
                    }

                    throw new IllegalStateException(
                            "No QueryHandler registered for query: " + type.getName()
                    );
                }
        );
    }

    private <R> QueryExecutionChain<R> buildInterceptorChain(
            QueryHandler<Query<R>, R> handler
    ) {
        QueryExecutionChain<R> chain =
                query -> handler.handle((Query<R>) query);

        for (int i = interceptors.size() - 1; i >= 0; i--) {
            QueryInterceptor interceptor = interceptors.get(i);
            QueryExecutionChain<R> next = chain;

            chain = query -> interceptor.intercept(query, next);
        }

        return chain;
    }
}