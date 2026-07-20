package com.kshitij.ratelimiter;

import com.kshitij.ratelimiter.model.RateLimitResult;
import com.kshitij.ratelimiter.service.RateLimitService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RateLimitServiceTest {
    @Autowired
    private RateLimitService rateLimitService;

    @Test
    public void tenAllowNextDeny(){
        for(int i = 0; i < 10; i++){
            RateLimitResult result=rateLimitService.tryConsume("client1");
            assertTrue(result.getAllowed(),"Failed at request: "+(i+1));
        }
        RateLimitResult result=rateLimitService.tryConsume("client1");
        assertFalse(result.getAllowed());
    }

    @Test
    public void testUpdateClientConfig() throws InterruptedException {
        rateLimitService.updateTokenBucket("update-config-client",20L,2L);
        for(int i = 0; i < 10; i++){
            RateLimitResult result=rateLimitService.tryConsume("update-config-client");
            assertTrue(result.getAllowed(),"Failed at request: "+(i+1));
        }
        RateLimitResult result1=rateLimitService.tryConsume("update-config-client");
        assertFalse(result1.getAllowed());
        Thread.sleep(5000);
        for(int i=10;i<20;i++){
            RateLimitResult result=rateLimitService.tryConsume("update-config-client");
            assertTrue(result.getAllowed(),"Failed at request: "+(i+1));
        }
        RateLimitResult result=rateLimitService.tryConsume("update-config-client");
        assertFalse(result.getAllowed());
    }

    @Test
    public void testConcurrentRequest() throws InterruptedException {
        String clientId="concurrent-testing";
        int capacity=10;
        int numberOfThreads=2000;

        rateLimitService.updateTokenBucket(clientId,(long) capacity,0L);

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch=new CountDownLatch(1);
        CountDownLatch doneLatch=new CountDownLatch(numberOfThreads);
        AtomicInteger allowCount=new  AtomicInteger(0);

        for(int i=0;i<numberOfThreads;i++){
            executorService.submit(()->{
                try{
                    startLatch.await();
                    RateLimitResult result=rateLimitService.tryConsume(clientId);
                    if(result.getAllowed()){
                        allowCount.incrementAndGet();
                    }
                } catch (InterruptedException e){
                    Thread.currentThread().interrupt();
                }finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();

        assertEquals(capacity,allowCount.get());
        executorService.shutdown();
    }

    @Test
    public void testSlidingWindow() throws InterruptedException {
        rateLimitService.updateSlidingWindow("sliding-window-client",2,5);
        for(int i=0;i<5;i++){
            RateLimitResult result=rateLimitService.tryConsume("sliding-window-client");
            assertTrue(result.getAllowed(),"Failed at request: "+(i+1));
        }
        RateLimitResult result=rateLimitService.tryConsume("sliding-window-client");
        assertFalse(result.getAllowed());
        Thread.sleep(2100);
        RateLimitResult result1=rateLimitService.tryConsume("sliding-window-client");
        assertTrue(result1.getAllowed());
    }
}
