package buildingblocks.core.queries;

@FunctionalInterface
public interface QueryExecutionChain<R> {
    R proceed(Query<R> query);
}