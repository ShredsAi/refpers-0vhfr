package ai.shreds.domain.value_objects;

import java.time.Duration;

import ai.shreds.shared.enums.SharedEnumRateLimitStrategy;

/**
 * Value object representing rate limiting configuration for a route.
 * Defines the maximum number of requests allowed within a time window
 * and the strategy used to enforce the limit.
 */
public class DomainValueRateLimitConfig {
    private final Integer maxRequests;
    private final Duration timeWindow;
    private final SharedEnumRateLimitStrategy strategy;
    
    private static final int MIN_REQUESTS = 1;
    private static final int MAX_REQUESTS = 1000000;
    private static final Duration MIN_WINDOW = Duration.ofSeconds(1);
    private static final Duration MAX_WINDOW = Duration.ofDays(1);

    public DomainValueRateLimitConfig(Integer maxRequests, Duration timeWindow, SharedEnumRateLimitStrategy strategy) {
        this.maxRequests = maxRequests;
        this.timeWindow = timeWindow;
        this.strategy = strategy;
        validate();
    }

    private void validate() {
        if (maxRequests == null) {
            throw new IllegalArgumentException("Max requests cannot be null");
        }
        if (maxRequests < MIN_REQUESTS || maxRequests > MAX_REQUESTS) {
            throw new IllegalArgumentException(String.format(
                "Max requests must be between %d and %d", MIN_REQUESTS, MAX_REQUESTS));
        }
        if (timeWindow == null) {
            throw new IllegalArgumentException("Time window cannot be null");
        }
        if (timeWindow.compareTo(MIN_WINDOW) < 0 || timeWindow.compareTo(MAX_WINDOW) > 0) {
            throw new IllegalArgumentException(String.format(
                "Time window must be between %s and %s", MIN_WINDOW, MAX_WINDOW));
        }
        if (strategy == null) {
            throw new IllegalArgumentException("Rate limit strategy cannot be null");
        }
    }

    public Integer getMaxRequests() {
        return maxRequests;
    }

    public Duration getTimeWindow() {
        return timeWindow;
    }

    public SharedEnumRateLimitStrategy getStrategy() {
        return strategy;
    }

    /**
     * Calculates the rate of requests allowed per second.
     *
     * @return requests per second as a double
     */
    public double getRequestsPerSecond() {
        return (double) maxRequests / timeWindow.getSeconds();
    }

    /**
     * Calculates the minimum interval between requests.
     *
     * @return the minimum duration between requests
     */
    public Duration getMinimumRequestInterval() {
        return Duration.ofNanos((long)(1_000_000_000L / getRequestsPerSecond()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DomainValueRateLimitConfig that = (DomainValueRateLimitConfig) o;
        return maxRequests.equals(that.maxRequests) &&
               timeWindow.equals(that.timeWindow) &&
               strategy == that.strategy;
    }

    @Override
    public int hashCode() {
        int result = maxRequests.hashCode();
        result = 31 * result + timeWindow.hashCode();
        result = 31 * result + strategy.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.format("RateLimitConfig{maxRequests=%d, timeWindow=%s, strategy=%s}",
            maxRequests, timeWindow, strategy);
    }
}