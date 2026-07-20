package com.kshitij.ratelimiter.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenBucket {
    private Long capacity;
    private Long refillRatePerSecond;
    private Double currentTokens;
    private Long lastRefillTimeStamp;

    public TokenBucket(Long capacity, Long refillRatePerSecond){
        this.capacity = capacity;
        this.refillRatePerSecond = refillRatePerSecond;
        this.currentTokens=capacity.doubleValue();
        this.lastRefillTimeStamp=System.currentTimeMillis();
    }
}
