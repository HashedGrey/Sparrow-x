package buildingblocks.core.queries;

public interface QueryInterceptor {

    <R> R intercept(
            Query<R> query,
            QueryExecutionChain<R> chain
    );
}