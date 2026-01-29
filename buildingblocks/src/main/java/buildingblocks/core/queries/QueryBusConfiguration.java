package buildingblocks.core.queries;

import buildingblocks.core.queries.interceptors.QueryInterceptor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class QueryBusConfiguration {

    @Bean
    public QueryBus queryBus(ApplicationContext context, List<QueryInterceptor> interceptors) {
        return new DefaultQueryBus(interceptors, context);
    }
}
