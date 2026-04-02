package buildingblocks.core.queries;

import java.util.concurrent.CompletableFuture;

public interface AsyncQueryBus {
    <Query extends buildingblocks.core.queries.Query<ResponseT>, ResponseT> CompletableFuture<ResponseT> send(Query query);
}
