package com.example.crud.metrics;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.jvm.BufferPoolMetricSet;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.codahale.metrics.logback.InstrumentedAppender;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

@Configuration
public class MetricsConfiguration {

    private static final String PROP_METRIC_REG_JVM_MEMORY = "jvm.memory";
    private static final String PROP_METRIC_REG_JVM_GARBAGE = "jvm.garbage";
    private static final String PROP_METRIC_REG_JVM_THREADS = "jvm.threads";
    private static final String PROP_METRIC_REG_JVM_FILES = "jvm.files";
    private static final String PROP_METRIC_REG_JVM_BUFFERS = "jvm.buffers";

    private final Logger log = (Logger) LoggerFactory.getLogger(getClass().getName());

    private final MetricRegistry metricRegistry = SharedMetricRegistries.getOrCreate("dunno");

    private final HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();

    @Bean
    public MetricRegistry metricRegistry() {
        return metricRegistry;
    }

    @Bean
    public HealthCheckRegistry healthCheckRegistry() {
        return healthCheckRegistry;
    }

    @PostConstruct
    public void init() {
        final LoggerContext factory = (LoggerContext) LoggerFactory.getILoggerFactory();
        final Logger root = factory.getLogger("console");

        final InstrumentedAppender metrics = new InstrumentedAppender(metricRegistry);
        metrics.setContext(root.getLoggerContext());
        metrics.start();
        root.addAppender(metrics);

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.reset();
        ContextInitializer initializer = new ContextInitializer(loggerContext);
        try {
            initializer.autoConfig();
        } catch (JoranException e) {
            log.error("Failed to reconfigure logging context", e);
        }

        log.debug("Registering JVM gauges");
        metricRegistry.registerAll(new OperatingSystemGaugeSet());
        metricRegistry.register(PROP_METRIC_REG_JVM_MEMORY, new MemoryUsageGaugeSet());
        metricRegistry.register(PROP_METRIC_REG_JVM_GARBAGE, new GarbageCollectorMetricSet());
        metricRegistry.register(PROP_METRIC_REG_JVM_THREADS, new ThreadStatesGaugeSet());
        metricRegistry.register(PROP_METRIC_REG_JVM_FILES, new FileDescriptorRatioGauge());
        metricRegistry.register(PROP_METRIC_REG_JVM_BUFFERS,
                new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));

        CoalescingReporter reporter = CoalescingReporter.forRegistry(metricRegistry)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(5, TimeUnit.SECONDS);
    }
}
