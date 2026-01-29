package buildingblocks.core.queries.interceptors;

import buildingblocks.core.queries.IQuery;
import buildingblocks.core.queries.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class QueryLoggingInterceptor implements QueryInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(QueryLoggingInterceptor.class);

    @Override
    public <Query extends IQuery<ResponseT>, ResponseT> ResponseT intercept(Query query, QueryHandler<Query, ResponseT> next) {
        long startTime = System.currentTimeMillis();
        logger.info("Handling query of type: {}", query.getClass().getSimpleName());
        logger.debug("Query details: {}", query);

        try {
            ResponseT response = next.handle(query);
            return response;
        } catch (Exception ex) {
            logger.error("Error executing query {}", query.getClass().getSimpleName(), ex);
            throw ex;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Query {} handled in {} ms", query.getClass().getSimpleName(), duration);
        }
    }
}
