package com.kshitij.ratelimiter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TokenBucketConfigRequest {
    @NotBlank(message="client Id is required")
    private String clientId;

    @NotNull(message="Capacity is required")
    @Positive(message = "Capacity should be positive")
    private Long capacity;

    @NotNull(message="Refill Rate is required")
    @Positive(message = "Rate should be positive")
    private Long refillRatePerSecond;
}
