package buildingblocks.core.queries.interceptors;

import buildingblocks.core.queries.IQuery;
import buildingblocks.core.queries.QueryHandler;

public interface QueryInterceptor {
    <Query extends IQuery<ResponseT>, ResponseT> ResponseT intercept(Query query, QueryHandler<Query, ResponseT> next);
}
