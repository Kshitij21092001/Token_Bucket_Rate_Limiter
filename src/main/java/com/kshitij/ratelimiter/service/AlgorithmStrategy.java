package com.kshitij.ratelimiter.service;

import com.kshitij.ratelimiter.model.RateLimitResult;

public interface AlgorithmStrategy {
    RateLimitResult tryConsume(String clientId);
}
