package com.kshitij.ratelimiter.service;

import com.kshitij.ratelimiter.model.RateLimitResult;
import com.kshitij.ratelimiter.model.SlidingWindow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SlidingWindowStrategy implements AlgorithmStrategy {
    private final Map<String, SlidingWindow> idToSlidingWindow=new ConcurrentHashMap<>();

    @Override
    public RateLimitResult tryConsume(String clientId){
        SlidingWindow slidingWindow=getSlidingWindowById(clientId);
        Deque<Long> clientDq= slidingWindow.getTimeStampDeque();
        synchronized (clientDq){
            Long currentTimeStamp=System.currentTimeMillis();
            Long windowStart=currentTimeStamp-(slidingWindow.getWindowSizeSeconds()*1000L);

            while(!clientDq.isEmpty() && clientDq.peekFirst()<windowStart){
                clientDq.pollFirst();
            }

            Boolean allowed=false;
            if(clientDq.size()< slidingWindow.getMaxRequests()){
                clientDq.addLast(currentTimeStamp);
                allowed=true;
            }
            Long remaining=(long)(slidingWindow.getMaxRequests()- clientDq.size());
            Long resetSeconds=0L;
            if(remaining==0L && !clientDq.isEmpty()){
                resetSeconds=(clientDq.peekFirst()+ (slidingWindow.getWindowSizeSeconds()*1000L)-currentTimeStamp)/1000L;
            }
            return new RateLimitResult(allowed,(long)slidingWindow.getMaxRequests(),remaining,resetSeconds);
        }
    }

    private SlidingWindow getSlidingWindowById(String clientId){
        return idToSlidingWindow.computeIfAbsent(clientId,k->new SlidingWindow(10,5));
    }

    public void updateClientConfig(String clientId,Integer windowSizeSeconds,Integer maxRequests){
        SlidingWindow slidingWindow=getSlidingWindowById(clientId);
        slidingWindow.setWindowSizeSeconds(windowSizeSeconds);
        slidingWindow.setMaxRequests(maxRequests);
    }
}
