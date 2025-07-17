package ai.shreds.domain.value_objects;

import java.time.Duration;

/**
 * Value object representing the rate at which tokens are refilled in a rate limit bucket.
 */
public class DomainValueRefillRate {
    private final double tokensPerSecond;
    private static final double MIN_TOKENS_PER_SECOND = 0.001; // 1 token per ~16.7 minutes
    private static final double MAX_TOKENS_PER_SECOND = 10000.0; // 10,000 tokens per second

    /**
     * Creates a new refill rate.
     *
     * @param tokensPerSecond the number of tokens to add per second
     */
    public DomainValueRefillRate(double tokensPerSecond) {
        this.tokensPerSecond = tokensPerSecond;
        validate();
    }

    private void validate() {
        if (tokensPerSecond <= 0) {
            throw new IllegalArgumentException("Tokens per second must be positive");
        }
        
        if (tokensPerSecond < MIN_TOKENS_PER_SECOND) {
            throw new IllegalArgumentException("Tokens per second must be at least " + MIN_TOKENS_PER_SECOND);
        }
        
        if (tokensPerSecond > MAX_TOKENS_PER_SECOND) {
            throw new IllegalArgumentException("Tokens per second cannot exceed " + MAX_TOKENS_PER_SECOND);
        }
        
        if (Double.isNaN(tokensPerSecond) || Double.isInfinite(tokensPerSecond)) {
            throw new IllegalArgumentException("Tokens per second must be a valid finite number");
        }
    }

    public double getTokensPerSecond() {
        return tokensPerSecond;
    }

    /**
     * Calculates the number of tokens to add based on the duration elapsed.
     *
     * @param duration the time duration elapsed
     * @return the number of tokens to add (rounded down to nearest integer)
     */
    public int calculateTokensForDuration(Duration duration) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            return 0;
        }
        
        double totalSeconds = duration.toMillis() / 1000.0;
        double tokensToAdd = tokensPerSecond * totalSeconds;
        
        // Return floor of tokens to avoid over-allocation
        return (int) Math.floor(tokensToAdd);
    }

    /**
     * Calculates the time needed to accumulate the specified number of tokens.
     *
     * @param tokens the number of tokens desired
     * @return the duration needed to accumulate the tokens
     */
    public Duration getTimeForTokens(int tokens) {
        if (tokens <= 0) {
            return Duration.ZERO;
        }
        
        double secondsRequired = tokens / tokensPerSecond;
        return Duration.ofMillis((long) (secondsRequired * 1000));
    }

    /**
     * Gets the interval between individual token additions.
     *
     * @return the duration between token additions
     */
    public Duration getTokenInterval() {
        return getTimeForTokens(1);
    }

    /**
     * Creates a refill rate from tokens per minute.
     *
     * @param tokensPerMinute tokens per minute
     * @return new DomainValueRefillRate instance
     */
    public static DomainValueRefillRate fromTokensPerMinute(double tokensPerMinute) {
        return new DomainValueRefillRate(tokensPerMinute / 60.0);
    }

    /**
     * Creates a refill rate from tokens per hour.
     *
     * @param tokensPerHour tokens per hour
     * @return new DomainValueRefillRate instance
     */
    public static DomainValueRefillRate fromTokensPerHour(double tokensPerHour) {
        return new DomainValueRefillRate(tokensPerHour / 3600.0);
    }

    /**
     * Creates a refill rate from a specific interval.
     *
     * @param tokensPerInterval number of tokens per interval
     * @param interval the time interval
     * @return new DomainValueRefillRate instance
     */
    public static DomainValueRefillRate fromInterval(int tokensPerInterval, Duration interval) {
        if (interval == null || interval.isZero() || interval.isNegative()) {
            throw new IllegalArgumentException("Interval must be positive");
        }
        
        if (tokensPerInterval <= 0) {
            throw new IllegalArgumentException("Tokens per interval must be positive");
        }
        
        double intervalSeconds = interval.toMillis() / 1000.0;
        double tokensPerSecond = tokensPerInterval / intervalSeconds;
        
        return new DomainValueRefillRate(tokensPerSecond);
    }

    /**
     * Gets the tokens per minute rate.
     *
     * @return tokens per minute
     */
    public double getTokensPerMinute() {
        return tokensPerSecond * 60.0;
    }

    /**
     * Gets the tokens per hour rate.
     *
     * @return tokens per hour
     */
    public double getTokensPerHour() {
        return tokensPerSecond * 3600.0;
    }

    /**
     * Checks if this refill rate is faster than another.
     *
     * @param other the other refill rate to compare
     * @return true if this rate is faster
     */
    public boolean isFasterThan(DomainValueRefillRate other) {
        if (other == null) {
            return true;
        }
        return this.tokensPerSecond > other.tokensPerSecond;
    }

    /**
     * Checks if this refill rate is slower than another.
     *
     * @param other the other refill rate to compare
     * @return true if this rate is slower
     */
    public boolean isSlowerThan(DomainValueRefillRate other) {
        if (other == null) {
            return false;
        }
        return this.tokensPerSecond < other.tokensPerSecond;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DomainValueRefillRate that = (DomainValueRefillRate) o;
        return Double.compare(that.tokensPerSecond, tokensPerSecond) == 0;
    }

    @Override
    public int hashCode() {
        return Double.valueOf(tokensPerSecond).hashCode();
    }

    @Override
    public String toString() {
        return String.format("RefillRate{%.3f tokens/second, %.1f tokens/minute, %.1f tokens/hour}", 
            tokensPerSecond, getTokensPerMinute(), getTokensPerHour());
    }
}