package com.kshitij.ratelimiter.service;

import com.kshitij.ratelimiter.enums.RateLimitAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RateLimitStrategyFactory {
    private final TokenBucketStrategy tokenBucketStrategy;
    private final SlidingWindowStrategy slidingWindowStrategy;

    public AlgorithmStrategy getStrategy(RateLimitAlgorithm rateLimitAlgorithm) {
        return switch (rateLimitAlgorithm) {
            case SLIDING_WINDOW -> slidingWindowStrategy;
            case TOKEN_BUCKET -> tokenBucketStrategy;
        };
    }
}
