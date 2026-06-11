//package buildingblocks.config;
//
//import buildingblocks.core.events.EventBus;
//import buildingblocks.infrastructure.persistence.UnitOfWork;
//import buildingblocks.infrastructure.persistence.UnitOfWorkImpl;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.transaction.PlatformTransactionManager;
//
//@Configuration
//public class BuildingBlocksConditionalConfiguration {
//
//    @Bean
//    @ConditionalOnBean({PlatformTransactionManager.class, EventBus.class})
//    public UnitOfWork unitOfWork(
//            PlatformTransactionManager transactionManager,
//            EventBus eventBus
//    ) {
//        return new UnitOfWorkImpl(transactionManager, eventBus);
//    }
//}