package com.kshitij.ratelimiter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SlidingWindowConfigRequest {
    @NotBlank(message = "client Id is Required")
    private String clientId;

    @NotNull(message = "window size is required")
    @Positive(message = "window size should be positive")
    private Integer windowSizeSeconds;

    @NotNull(message = "max request val is required")
    @Positive(message = "maximum requests should be positive")
    private Integer maxRequests;
}
