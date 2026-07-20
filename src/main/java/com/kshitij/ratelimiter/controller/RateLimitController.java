package com.kshitij.ratelimiter.controller;

import com.kshitij.ratelimiter.dto.SlidingWindowConfigRequest;
import com.kshitij.ratelimiter.dto.TokenBucketConfigRequest;
import com.kshitij.ratelimiter.dto.RateLimitRequest;
import com.kshitij.ratelimiter.dto.RateLimitResponse;
import com.kshitij.ratelimiter.model.RateLimitResult;
import com.kshitij.ratelimiter.service.RateLimitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rate-limiter")
public class RateLimitController {
    private final RateLimitService rateLimitService;

    @PostMapping("/check")
    public ResponseEntity<RateLimitResponse> checkRateLimit(@RequestBody @Valid RateLimitRequest rateLimitRequest) {
        RateLimitResult result=rateLimitService.tryConsume(rateLimitRequest.getClientId());
        HttpStatus status=result.getAllowed()?HttpStatus.ACCEPTED:HttpStatus.TOO_MANY_REQUESTS;
        String body=result.getAllowed()?"ALLOW":"DENY";

        return ResponseEntity.status(status)
                .header("X-RateLimit-Limit",String.valueOf(result.getLimit()))
                .header("X-RateLimit-Remaining", String.valueOf(result.getRemaining()))
                .header("X-RateLimit-ResetSeconds",String.valueOf(result.getResetSeconds()))
                .body(new RateLimitResponse(body));
    }

    @PostMapping("/admin/config/token-bucket")
    public ResponseEntity<Void> updateTokenBucket(@RequestBody @Valid TokenBucketConfigRequest adminConfigRequest) {
        rateLimitService.updateTokenBucket(adminConfigRequest.getClientId(), adminConfigRequest.getCapacity(),
                adminConfigRequest.getRefillRatePerSecond());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/config/sliding-window")
    public ResponseEntity<Void> updateSlidingWindow(@RequestBody @Valid SlidingWindowConfigRequest slidingWindowConfigRequest) {
        rateLimitService.updateSlidingWindow(slidingWindowConfigRequest.getClientId(), slidingWindowConfigRequest.getWindowSizeSeconds(),
                slidingWindowConfigRequest.getMaxRequests());
        return ResponseEntity.ok().build();
    }
}
