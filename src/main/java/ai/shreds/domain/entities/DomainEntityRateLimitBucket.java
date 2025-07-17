package ai.shreds.domain.entities;

import java.time.Duration;
import java.time.Instant;

import ai.shreds.domain.value_objects.DomainValueBucketId;
import ai.shreds.domain.value_objects.DomainValueRefillRate;
import ai.shreds.shared.value_objects.SharedValueUserId;
import ai.shreds.shared.value_objects.SharedValueRouteId;

/**
 * Entity representing a token bucket for rate limiting.
 * Implements token bucket algorithm for controlling request rates.
 */
public class DomainEntityRateLimitBucket {
    private final DomainValueBucketId bucketId;
    private final SharedValueUserId userId;
    private final SharedValueRouteId routeId;
    private int currentTokens;
    private final int maxTokens;
    private final DomainValueRefillRate refillRate;
    private Instant lastRefillTime;

    /**
     * Creates a new rate limit bucket.
     *
     * @param bucketId unique identifier for this bucket
     * @param userId user identifier this bucket applies to (null for global buckets)
     * @param routeId route identifier this bucket applies to
     * @param currentTokens current number of available tokens
     * @param maxTokens maximum number of tokens this bucket can hold
     * @param refillRate rate at which tokens are refilled
     * @param lastRefillTime last time tokens were refilled
     */
    public DomainEntityRateLimitBucket(
            DomainValueBucketId bucketId,
            SharedValueUserId userId,
            SharedValueRouteId routeId,
            int currentTokens,
            int maxTokens,
            DomainValueRefillRate refillRate,
            Instant lastRefillTime) {
        this.bucketId = bucketId;
        this.userId = userId;
        this.routeId = routeId;
        this.currentTokens = currentTokens;
        this.maxTokens = maxTokens;
        this.refillRate = refillRate;
        this.lastRefillTime = lastRefillTime;
        validate();
    }

    private void validate() {
        if (bucketId == null) {
            throw new IllegalArgumentException("Bucket ID cannot be null");
        }
        
        // userId can be null for global rate limits
        
        if (routeId == null) {
            throw new IllegalArgumentException("Route ID cannot be null");
        }
        
        if (currentTokens < 0) {
            throw new IllegalArgumentException("Current tokens cannot be negative");
        }
        
        if (maxTokens <= 0) {
            throw new IllegalArgumentException("Maximum tokens must be positive");
        }
        
        if (currentTokens > maxTokens) {
            throw new IllegalArgumentException("Current tokens cannot exceed maximum tokens");
        }
        
        if (refillRate == null) {
            throw new IllegalArgumentException("Refill rate cannot be null");
        }
        
        if (lastRefillTime == null) {
            throw new IllegalArgumentException("Last refill time cannot be null");
        }
    }

    public DomainValueBucketId getBucketId() {
        return bucketId;
    }

    public SharedValueUserId getUserId() {
        return userId;
    }

    public SharedValueRouteId getRouteId() {
        return routeId;
    }

    public int getCurrentTokens() {
        return currentTokens;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public DomainValueRefillRate getRefillRate() {
        return refillRate;
    }

    public Instant getLastRefillTime() {
        return lastRefillTime;
    }

    /**
     * Attempts to consume a token from the bucket.
     * 
     * @return true if token was consumed, false if no tokens were available
     */
    public synchronized boolean consumeToken() {
        refillTokens(); // Refill tokens first based on elapsed time
        
        if (currentTokens > 0) {
            currentTokens--;
            return true;
        }
        
        return false;
    }

    /**
     * Refills tokens in the bucket based on elapsed time since last refill.
     */
    public synchronized void refillTokens() {
        Instant now = Instant.now();
        Duration elapsedTime = Duration.between(lastRefillTime, now);
        
        if (elapsedTime.isZero() || elapsedTime.isNegative()) {
            return; // No time has passed or clock skew detected
        }
        
        int tokensToAdd = calculateTokensToAdd(elapsedTime);
        if (tokensToAdd > 0) {
            currentTokens = Math.min(currentTokens + tokensToAdd, maxTokens);
            lastRefillTime = now;
        }
    }

    /**
     * Calculates how many tokens to add based on elapsed time and refill rate.
     *
     * @param elapsedTime time elapsed since last refill
     * @return number of tokens to add
     */
    private int calculateTokensToAdd(Duration elapsedTime) {
        return refillRate.calculateTokensForDuration(elapsedTime);
    }

    /**
     * Sets the current tokens to zero (drains the bucket).
     */
    public void drainBucket() {
        currentTokens = 0;
    }

    /**
     * Checks if the bucket is empty.
     *
     * @return true if there are no tokens available
     */
    public boolean isEmpty() {
        refillTokens(); // Make sure we're current
        return currentTokens == 0;
    }

    /**
     * Gets the time until the next token will be available.
     *
     * @return duration until next token, or zero if tokens are available now
     */
    public Duration getTimeUntilNextToken() {
        if (currentTokens > 0) {
            return Duration.ZERO;
        }
        
        double secondsPerToken = 1.0 / refillRate.getTokensPerSecond();
        return Duration.ofNanos((long)(secondsPerToken * 1_000_000_000));
    }

    @Override
    public String toString() {
        return String.format("RateLimitBucket{bucketId=%s, userId=%s, routeId=%s, tokens=%d/%d, refillRate=%s}", 
            bucketId, userId, routeId, currentTokens, maxTokens, refillRate);
    }
}
