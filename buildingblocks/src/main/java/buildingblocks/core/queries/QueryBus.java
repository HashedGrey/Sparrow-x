package buildingblocks.core.queries;

public interface QueryBus {
    <R> R dispatch(Query<R> query);
}