package com.sparrowx.agentic.config;

import com.sparrowx.agentic.temporal.activity.MissionActivities;
import com.sparrowx.agentic.temporal.workflow.MissionWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(
        prefix = "sparrowx.agentic.temporal",
        name = "worker-enabled",
        havingValue = "true",
        matchIfMissing = true
)
public final class TemporalWorkerConfig {

    @Bean
    public WorkerFactory temporalWorkerFactory(
            WorkflowClient workflowClient
    ) {
        return WorkerFactory.newInstance(workflowClient);
    }

    @Bean
    public Worker missionTemporalWorker(
            WorkerFactory workerFactory,
            MissionActivities missionActivities,
            TemporalProperties properties
    ) {
        WorkerOptions options = WorkerOptions.newBuilder()
                .setMaxConcurrentWorkflowTaskExecutionSize(
                        properties.maxConcurrentWorkflowTaskExecutions()
                )
                .setMaxConcurrentActivityExecutionSize(
                        properties.maxConcurrentActivityExecutions()
                )
                .setMaxConcurrentWorkflowTaskPollers(
                        properties.maxConcurrentWorkflowTaskPollers()
                )
                .setMaxConcurrentActivityTaskPollers(
                        properties.maxConcurrentActivityTaskPollers()
                )
                .build();

        Worker worker = workerFactory.newWorker(
                properties.taskQueue(),
                options
        );
        worker.registerWorkflowImplementationTypes(
                MissionWorkflowImpl.class
        );
        worker.registerActivitiesImplementations(missionActivities);
        return worker;
    }

    @Bean
    public SmartLifecycle temporalWorkerLifecycle(
            WorkerFactory workerFactory,
            Worker missionTemporalWorker
    ) {
        Objects.requireNonNull(
                missionTemporalWorker,
                "missionTemporalWorker must not be null"
        );
        return new WorkerFactoryLifecycle(workerFactory);
    }

    private static final class WorkerFactoryLifecycle
            implements SmartLifecycle {

        private final WorkerFactory workerFactory;
        private final AtomicBoolean running = new AtomicBoolean(false);

        private WorkerFactoryLifecycle(WorkerFactory workerFactory) {
            this.workerFactory = Objects.requireNonNull(
                    workerFactory,
                    "workerFactory must not be null"
            );
        }

        @Override
        public void start() {
            if (running.compareAndSet(false, true)) {
                workerFactory.start();
            }
        }

        @Override
        public void stop() {
            if (running.compareAndSet(true, false)) {
                workerFactory.shutdown();
            }
        }

        @Override
        public void stop(Runnable callback) {
            try {
                stop();
            } finally {
                callback.run();
            }
        }

        @Override
        public boolean isRunning() {
            return running.get();
        }

        @Override
        public boolean isAutoStartup() {
            return true;
        }

        @Override
        public int getPhase() {
            return Integer.MAX_VALUE;
        }
    }
}
