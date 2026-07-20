package com.kshitij.ratelimiter.service;

import com.kshitij.ratelimiter.enums.RateLimitAlgorithm;
import com.kshitij.ratelimiter.model.RateLimitResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class RateLimitService {
    private final Map<String, RateLimitAlgorithm> rateLimitAlgorithmMap=new ConcurrentHashMap<>();
    private final RateLimitStrategyFactory rateLimitStrategyFactory;
    private final TokenBucketStrategy tokenBucketStrategy;
    private final SlidingWindowStrategy slidingWindowStrategy;

    public RateLimitResult tryConsume(String clientId) {
        RateLimitAlgorithm rateLimitAlgorithm=rateLimitAlgorithmMap.computeIfAbsent(clientId,k->RateLimitAlgorithm.TOKEN_BUCKET);
        AlgorithmStrategy strategy=rateLimitStrategyFactory.getStrategy(rateLimitAlgorithm);
        return strategy.tryConsume(clientId);
    }

    public void updateTokenBucket(String clientId, Long capacity, Long refillRatePerSecond){
        rateLimitAlgorithmMap.put(clientId,RateLimitAlgorithm.TOKEN_BUCKET);
        tokenBucketStrategy.updateClientConfig(clientId,capacity,refillRatePerSecond);
    }

    public void updateSlidingWindow(String clientId,Integer windowSizeSeconds,Integer maxRequests){
        rateLimitAlgorithmMap.put(clientId,RateLimitAlgorithm.SLIDING_WINDOW);
        slidingWindowStrategy.updateClientConfig(clientId,windowSizeSeconds,maxRequests);
    }
}
