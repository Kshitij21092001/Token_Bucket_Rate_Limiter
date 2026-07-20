package com.kshitij.ratelimiter.service;

import com.kshitij.ratelimiter.entity.TokenBucketEntity;
import com.kshitij.ratelimiter.model.RateLimitResult;
import com.kshitij.ratelimiter.model.TokenBucket;
import com.kshitij.ratelimiter.repository.TokenBucketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class TokenBucketStrategy implements AlgorithmStrategy {
    private final Map<String, TokenBucket> idToTokenBucket=new ConcurrentHashMap<>();
    private final TokenBucketRepository tokenBucketRepository;

    @Override
    public RateLimitResult tryConsume(String clientId){
        TokenBucket clientBucket=getTokenBucketById(clientId);
        synchronized (clientBucket){//check why we didn't use reentrant lock here
            refill(clientBucket);//To refill bucket from last timestamp
            Boolean allowed=clientBucket.getCurrentTokens()>=1;
            if(allowed)clientBucket.setCurrentTokens(clientBucket.getCurrentTokens()-1);
            Long remaining=(long) Math.floor(clientBucket.getCurrentTokens());
            Long resetSeconds=0L;
            if(remaining<1L){
                resetSeconds=(long)Math.ceil((1.0- clientBucket.getCurrentTokens())/clientBucket.getRefillRatePerSecond());
            }
            return new RateLimitResult(allowed, clientBucket.getCapacity(), remaining, resetSeconds);
        }
    }

    private TokenBucket getTokenBucketById(String clientId){
        return idToTokenBucket.computeIfAbsent(clientId,id->{
           TokenBucketEntity entity=tokenBucketRepository.findById(clientId).orElse(null);
           if(entity!=null){
               return toModel(entity);
           }
           TokenBucketEntity newEntity=toEntity(new TokenBucket(10L,2L),clientId);
           tokenBucketRepository.save(newEntity);
           return toModel(newEntity);
        });
    }

    private void refill(TokenBucket clientBucket){
        Long currentTime=System.currentTimeMillis();
        Long prevTime=clientBucket.getLastRefillTimeStamp();

        Long refillRate=clientBucket.getRefillRatePerSecond();
        Double timeLapsed=(currentTime-prevTime)/1000.0;

        Double totalTokens= clientBucket.getCurrentTokens()+refillRate*timeLapsed;
        clientBucket.setCurrentTokens(Math.min(totalTokens,clientBucket.getCapacity()));
        clientBucket.setLastRefillTimeStamp(currentTime);
    }

    public void updateClientConfig(String clientId,Long capacity,Long refillRatePerSecond){
        TokenBucket clientBucket=getTokenBucketById(clientId);
        clientBucket.setCapacity(capacity);
        clientBucket.setRefillRatePerSecond(refillRatePerSecond);
        clientBucket.setCurrentTokens(Math.min(clientBucket.getCurrentTokens(),capacity));
    }

    @Scheduled(fixedRate = 5000)
    private void updateDB(){
        for(Map.Entry<String, TokenBucket> entry:idToTokenBucket.entrySet()){
            //save will upsert, means if entry is there then update or else create new
            tokenBucketRepository.save(toEntity(entry.getValue(), entry.getKey()));
        }
    }

    private TokenBucket toModel(TokenBucketEntity tokenBucketEntity){
        TokenBucket tokenBucket=new  TokenBucket(tokenBucketEntity.getCapacity(), tokenBucketEntity.getRefillRatePerSecond());
        tokenBucket.setLastRefillTimeStamp(tokenBucketEntity.getLastRefillTimeStamp());
        tokenBucket.setCurrentTokens(tokenBucketEntity.getCurrentTokens());
        return  tokenBucket;
    }

    private TokenBucketEntity toEntity(TokenBucket tokenBucket, String clientId){
        TokenBucketEntity entity=new TokenBucketEntity();
        entity.setClientId(clientId);
        entity.setCapacity(tokenBucket.getCapacity());
        entity.setRefillRatePerSecond(tokenBucket.getRefillRatePerSecond());
        entity.setLastRefillTimeStamp(tokenBucket.getLastRefillTimeStamp());
        entity.setCurrentTokens(tokenBucket.getCurrentTokens());
        return entity;
    }
}
