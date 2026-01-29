package buildingblocks.core.queries;

public interface QueryBus {
    <Query extends IQuery<ResponseT>, ResponseT> ResponseT send(Query query);
}
