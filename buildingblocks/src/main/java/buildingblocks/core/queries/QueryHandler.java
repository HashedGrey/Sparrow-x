package buildingblocks.core.queries;

public interface QueryHandler<TQuery extends Query<R>, R> {
    R handle(TQuery query);
}