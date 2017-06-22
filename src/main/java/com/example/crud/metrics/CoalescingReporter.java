package com.example.crud.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

/**
 * A reporter class for logging metrics values to a {@link Logger} periodically, similar to
 * {@link com.codahale.metrics.ConsoleReporter} or {@link com.codahale.metrics.CsvReporter}, but using
 * the logging framework instead. It also supports specifying a {@link Marker} instance that can be used
 * by custom appenders and filters for the bound logging toolkit to further process metrics reports.
 */
public final class CoalescingReporter extends ScheduledReporter {

    private final Logger logger;
    private final Marker marker;

    /**
     * Returns a new {@link Builder} for {@link CoalescingReporter}.
     *
     * @param registry the registry to report
     * @return a {@link Builder} instance for a {@link CoalescingReporter}
     */
    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }

    private CoalescingReporter(MetricRegistry registry,
            Logger logger,
            Marker marker,
            TimeUnit rateUnit,
            TimeUnit durationUnit,
            MetricFilter filter) {
        super(registry, "logger-reporter", filter, rateUnit, durationUnit);
        this.logger = logger;
        this.marker = marker;
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges,
            SortedMap<String, Counter> counters,
            SortedMap<String, Histogram> histograms,
            SortedMap<String, Meter> meters,
            SortedMap<String, Timer> timers) {
        StringBuilder data = new StringBuilder();
        for (Entry<String, Gauge> entry : gauges.entrySet()) {
            addGauge(data, entry.getKey(), entry.getValue());
        }

        for (Entry<String, Counter> entry : counters.entrySet()) {
            addCounter(data, entry.getKey(), entry.getValue());
        }

        for (Entry<String, Histogram> entry : histograms.entrySet()) {
            addHistogram(data, entry.getKey(), entry.getValue());
        }

        for (Entry<String, Meter> entry : meters.entrySet()) {
            addMeter(data, entry.getKey(), entry.getValue());
        }

        for (Entry<String, Timer> entry : timers.entrySet()) {
            addTimer(data, entry.getKey(), entry.getValue());
        }
        logger.info(marker, data.toString());
    }

    private void addTimer(StringBuilder data, String name, Timer timer) {
        final Snapshot snapshot = timer.getSnapshot();
        data.append(" type=timer.").append(name).append(":");
        data.append(" count=").append(timer.getCount());
        data.append(", min=").append(convertDuration(snapshot.getMin()));
        data.append(", max=").append(convertDuration(snapshot.getMax()));
        data.append(", mean=").append(convertDuration(snapshot.getMean()));
        data.append(", stdDev=").append(convertDuration(snapshot.getStdDev()));
        data.append(", median=").append(convertDuration(snapshot.getMedian()));
        data.append(", p75=").append(convertDuration(snapshot.get75thPercentile()));
        data.append(", p95=").append(convertDuration(snapshot.get95thPercentile()));
        data.append(", p98=").append(convertDuration(snapshot.get98thPercentile()));
        data.append(", p99=").append(convertDuration(snapshot.get99thPercentile()));
        data.append(", 999=").append(convertDuration(snapshot.get999thPercentile()));
        data.append(", mean_rate=").append(convertRate(timer.getMeanRate()));
        data.append(", m1=").append(convertRate(timer.getMeanRate()));
        data.append(", m5=").append(convertRate(timer.getMeanRate()));
        data.append(", m15=").append(convertRate(timer.getMeanRate()));
        data.append(", rate_unit=").append(getRateUnit());
        data.append(", duration_unit=").append(getDurationUnit());
    }

    private void addMeter(StringBuilder data, String name, Meter meter) {
        data.append(" type=meter.").append(name).append(":");
        data.append(" count=").append(meter.getCount());
        data.append(", mean_rate=").append(convertRate(meter.getMeanRate()));
        data.append(", m1=").append(convertRate(meter.getOneMinuteRate()));
        data.append(", m5=").append(convertRate(meter.getFiveMinuteRate()));
        data.append(", m15=").append(convertRate(meter.getFifteenMinuteRate()));
        data.append(", rate_unit=").append(getRateUnit());
    }

    private void addHistogram(StringBuilder data, String name, Histogram histogram) {
        final Snapshot snapshot = histogram.getSnapshot();
        data.append(" type=histogram.").append(name).append(":");
        data.append(" count=").append(histogram.getCount());
        data.append(", min=").append(snapshot.getMin());
        data.append(", max=").append(snapshot.getMax());
        data.append(", mean=").append(snapshot.getMean());
        data.append(", stdDev=").append(snapshot.getStdDev());
        data.append(", median=").append(snapshot.getMedian());
        data.append(", p75=").append(snapshot.get75thPercentile());
        data.append(", p95=").append(snapshot.get95thPercentile());
        data.append(", p98=").append(snapshot.get98thPercentile());
        data.append(", p99=").append(snapshot.get99thPercentile());
        data.append(", 999=").append(snapshot.get999thPercentile());
    }

    private void addCounter(StringBuilder data, String name, Counter counter) {
        data.append(" counter.").append(name).append(": ").append(counter.getCount());
    }

    private void addGauge(StringBuilder data, String name, Gauge gauge) {
        data.append(" gauge.").append(name).append(": ").append(gauge.getValue());
    }

    @Override
    protected String getRateUnit() {
        return "events/" + super.getRateUnit();
    }

    /**
     * A builder for {@link com.codahale.metrics.CsvReporter} instances. Defaults to logging to {@code metrics}, not
     * using a marker, converting rates to events/second, converting durations to milliseconds, and
     * not filtering metrics.
     */
    public static final class Builder {
        private final MetricRegistry registry;
        private Logger logger;
        private Marker marker;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.logger = LoggerFactory.getLogger("metrics");
            this.marker = null;
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
        }

        /**
         * Log metrics to the given logger.
         *
         * @param logger a {@link Logger}
         * @return {@code this}
         */
        public Builder outputTo(Logger logger) {
            this.logger = logger;
            return this;
        }

        /**
         * Mark all logged metrics with the given marker.
         *
         * @param marker a {@link Marker}
         * @return {@code this}
         */
        public Builder markWith(Marker marker) {
            this.marker = marker;
            return this;
        }

        /**
         * Convert rates to the given time unit.
         *
         * @param rateUnit a unit of time
         * @return {@code this}
         */
        public Builder convertRatesTo(TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        /**
         * Convert durations to the given time unit.
         *
         * @param durationUnit a unit of time
         * @return {@code this}
         */
        public Builder convertDurationsTo(TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return this;
        }

        /**
         * Only report metrics which match the given filter.
         *
         * @param filter a {@link MetricFilter}
         * @return {@code this}
         */
        public Builder filter(MetricFilter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Builds a {@link CoalescingReporter} with the given properties.
         *
         * @return a {@link CoalescingReporter}
         */
        public CoalescingReporter build() {
            return new CoalescingReporter(registry, logger, marker, rateUnit, durationUnit, filter);
        }
    }

}
