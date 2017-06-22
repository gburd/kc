package com.example.crud.metrics;

import com.ryantenney.metrics.spring.reporter.AbstractScheduledReporterFactoryBean;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import java.util.concurrent.TimeUnit;

/**
 * CoalescingReporterFactoryBean.
 */
public class CoalescingReporterFactoryBean extends AbstractScheduledReporterFactoryBean<CoalescingReporter> {

    /** Period attribute. */
    public static final String PERIOD = "period";
    /** Duration unit. */
    public static final String DURATION_UNIT = "duration-unit";
    /** Rate unit. */
    public static final String RATE_UNIT = "rate-unit";
    /** Marker. */
    public static final String MARKER = "marker";
    /** Logger. */
    public static final String LOGGER = "logger";

    @Override
    public Class<CoalescingReporter> getObjectType() {
        return CoalescingReporter.class;
    }

    @Override
    protected CoalescingReporter createInstance() {
        final CoalescingReporter.Builder reporter = CoalescingReporter.forRegistry(getMetricRegistry());
        if (hasProperty(DURATION_UNIT)) {
            reporter.convertDurationsTo(getProperty(DURATION_UNIT, TimeUnit.class));
        }
        if (hasProperty(RATE_UNIT)) {
            reporter.convertRatesTo(getProperty(RATE_UNIT, TimeUnit.class));
        }
        reporter.filter(getMetricFilter());
        if (hasProperty(MARKER)) {
            reporter.markWith(MarkerFactory.getMarker(getProperty(MARKER)));
        }
        if (hasProperty(LOGGER)) {
            reporter.outputTo(LoggerFactory.getLogger(getProperty(LOGGER)));
        }
        return reporter.build();
    }

    @Override
    protected long getPeriod() {
        return convertDurationString(getProperty(PERIOD));
    }

}
