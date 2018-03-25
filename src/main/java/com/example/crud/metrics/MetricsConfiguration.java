package com.example.crud.metrics;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import com.codahale.metrics.*;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.jvm.*;
import com.datastax.driver.core.Cluster;
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurerAdapter;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableMetrics(proxyTargetClass = true)
public class MetricsConfiguration extends MetricsConfigurerAdapter {

    private static final String PROP_METRIC_REG_JVM_MEMORY = "jvm.memory";
    private static final String PROP_METRIC_REG_JVM_GARBAGE = "jvm.garbage";
    private static final String PROP_METRIC_REG_JVM_THREADS = "jvm.threads";
    private static final String PROP_METRIC_REG_JVM_FILES = "jvm.files";
    private static final String PROP_METRIC_REG_JVM_BUFFERS = "jvm.buffers";

    private final Logger log = (Logger)LoggerFactory.getLogger(getClass().getName());

    private MetricRegistry metricRegistry = SharedMetricRegistries.getOrCreate("dunno");

    private HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();

    @Autowired
    ApplicationContext context;

    @Override
    @Bean
    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    @Override
    @Bean
    public HealthCheckRegistry getHealthCheckRegistry() {
        return healthCheckRegistry;
    }

    @PostConstruct
    public void init() {
/*
        final LoggerContext factory = (LoggerContext) LoggerFactory.getILoggerFactory();
        final Logger root = factory.getLogger("console");//Logger.ROOT_LOGGER_NAME);

        final InstrumentedAppender metrics = new InstrumentedAppender();
        metrics.setContext(root.getLoggerContext());
        metrics.start();
        root.addAppender(metrics);
*/

        LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
        loggerContext.reset();
        ContextInitializer initializer = new ContextInitializer(loggerContext);
        try {
            initializer.autoConfig();
        } catch (JoranException e) {
            e.printStackTrace();
        }


        log.debug("Registering JVM gauges");
        metricRegistry.registerAll(new OperatingSystemGaugeSet());
        metricRegistry.register(PROP_METRIC_REG_JVM_MEMORY, new MemoryUsageGaugeSet());
        metricRegistry.register(PROP_METRIC_REG_JVM_GARBAGE, new GarbageCollectorMetricSet());
        metricRegistry.register(PROP_METRIC_REG_JVM_THREADS, new ThreadStatesGaugeSet());
        metricRegistry.register(PROP_METRIC_REG_JVM_FILES, new FileDescriptorRatioGauge());
        metricRegistry.register(PROP_METRIC_REG_JVM_BUFFERS,
                new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));

        Cluster cluster = context.getBean(Cluster.class);
        cluster.getMetrics().getRegistry().addListener(
                new com.codahale.metrics.MetricRegistryListener() {
                    
                    private final String METRIC_NAME_PREFIX = "com.datastax.";

                    @Override
                    public void onGaugeAdded(String name, Gauge<?> gauge) {
                        //if (metricRegistry.getNames().contains(name)) {
                        // name is already taken, maybe prefix with a namespace
                        //} else {
                        metricRegistry.register(METRIC_NAME_PREFIX + name, gauge);
                    }

                    @Override
                    public void onGaugeRemoved(String name) {
                    }

                    @Override
                    public void onCounterAdded(String name, Counter counter) {
                        metricRegistry.register(METRIC_NAME_PREFIX + name, counter);
                    }

                    @Override
                    public void onCounterRemoved(String name) {
                    }

                    @Override
                    public void onHistogramAdded(String name, Histogram histogram) {
                        metricRegistry.register(METRIC_NAME_PREFIX + name, histogram);
                    }

                    @Override
                    public void onHistogramRemoved(String name) {
                    }

                    @Override
                    public void onMeterAdded(String name, Meter meter) {
                        metricRegistry.register(METRIC_NAME_PREFIX + name, meter);
                    }

                    @Override
                    public void onMeterRemoved(String name) {
                    }

                    @Override
                    public void onTimerAdded(String name, Timer timer) {
                        metricRegistry.register(METRIC_NAME_PREFIX + name, timer);
                    }

                    @Override
                    public void onTimerRemoved(String name) {
                    }
                });
        CoalescingReporter reporter = CoalescingReporter.forRegistry(metricRegistry)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(5, TimeUnit.SECONDS);
    }

}
