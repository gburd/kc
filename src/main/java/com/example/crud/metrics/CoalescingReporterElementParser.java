package com.example.crud.metrics;

import com.ryantenney.metrics.spring.reporter.AbstractReporterElementParser;

/**
 * Reporter for metrics-spring which logs more compact, all in one line instead of one line for each metric.
 */
public class CoalescingReporterElementParser extends AbstractReporterElementParser {

    private static final String FILTER_REF = "filter-ref";
    private static final String FILTER_PATTERN = "filter";

    @Override
    public String getType() {
        return "compact-slf4j";
    }

    @Override
    protected Class<?> getBeanClass() {
        return CoalescingReporterFactoryBean.class;
    }

    @Override
    protected void validate(ValidationContext c) {
        c.require(CoalescingReporterFactoryBean.PERIOD, DURATION_STRING_REGEX, "Period is required and must be in the form '\\d+(ns|us|ms|s|m|h|d)'");
        c.optional(CoalescingReporterFactoryBean.MARKER);
        c.optional(CoalescingReporterFactoryBean.LOGGER);
        c.optional(CoalescingReporterFactoryBean.RATE_UNIT, TIMEUNIT_STRING_REGEX, "Rate unit must be one of the enum constants from java.util.concurrent.TimeUnit");
        c.optional(CoalescingReporterFactoryBean.DURATION_UNIT, TIMEUNIT_STRING_REGEX, "Duration unit must be one of the enum constants from java.util.concurrent.TimeUnit");
        c.optional(FILTER_PATTERN);
        c.optional(FILTER_REF);
        if (c.has(FILTER_PATTERN) && c.has(FILTER_REF)) {
            c.reject(FILTER_REF, "Reporter element must not specify both the 'filter' and 'filter-ref' attributes");
        }
        c.rejectUnmatchedProperties();
    }

}
