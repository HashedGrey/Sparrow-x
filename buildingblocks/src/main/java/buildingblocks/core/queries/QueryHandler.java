package buildingblocks.core.queries;

public interface QueryHandler<Query extends IQuery<ResponseT>, ResponseT> {
    ResponseT handle(Query query);
}
