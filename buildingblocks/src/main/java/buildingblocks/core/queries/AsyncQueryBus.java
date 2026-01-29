package buildingblocks.core.queries;

import java.util.concurrent.CompletableFuture;

public interface AsyncQueryBus {
    <Query extends IQuery<ResponseT>, ResponseT> CompletableFuture<ResponseT> send(Query query);
}
