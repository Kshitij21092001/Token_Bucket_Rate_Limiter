package com.kshitij.ratelimiter.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RateLimitResult {
    private Boolean allowed;
    private Long limit;//maximum capacity
    private Long remaining;//remaining tokens/requests
    private Long resetSeconds;//time in which will get at least one token
}
