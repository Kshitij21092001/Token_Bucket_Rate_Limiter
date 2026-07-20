package com.kshitij.ratelimiter.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "token_buckets")
public class TokenBucketEntity {
    @Id
    private String clientId;

    @Column(nullable = false)
    private Long capacity;

    @Column(nullable = false)
    private Long refillRatePerSecond;

    @Column(nullable = false)
    private Double currentTokens;

    @Column(nullable = false)
    private Long lastRefillTimeStamp;
}
