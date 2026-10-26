package com.pravinmali.orderprocessing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async configuration for domain event processing.
 *
 * <p>Enables Spring's asynchronous method execution via {@code @EnableAsync}.
 * Defines a dedicated thread pool for order domain event listeners so that
 * event handling does not block the HTTP request threads.</p>
 *
 * <p>The thread pool parameters are externalised to {@code application.properties}
 * under the {@code app.async.*} namespace so they can be tuned per environment.</p>
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Value("${app.async.core-pool-size:2}")
    private int corePoolSize;

    @Value("${app.async.max-pool-size:5}")
    private int maxPoolSize;

    @Value("${app.async.queue-capacity:100}")
    private int queueCapacity;

    @Value("${app.async.thread-name-prefix:order-event-}")
    private String threadNamePrefix;

    /**
     * Named executor used by {@code @Async("orderEventExecutor")} on event listeners.
     *
     * <p>Using a named executor (rather than the default) gives explicit control over
     * pool sizing and makes it easy to identify event-processing threads in logs and
     * thread dumps.</p>
     */
    @Bean(name = "orderEventExecutor")
    public Executor orderEventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        // Ensures graceful shutdown: waits for in-flight tasks before JVM exits
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
