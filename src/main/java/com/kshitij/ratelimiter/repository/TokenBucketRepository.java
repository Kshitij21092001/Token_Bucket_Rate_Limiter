package com.kshitij.ratelimiter.repository;

import com.kshitij.ratelimiter.entity.TokenBucketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenBucketRepository extends JpaRepository<TokenBucketEntity,String> {
}
