package com.kshitij.ratelimiter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kshitij.ratelimiter.dto.RateLimitRequest;
import com.kshitij.ratelimiter.service.RateLimitService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LoadTest {
    @LocalServerPort
    private int port;

    @Autowired
    private RateLimitService rateLimitService;

    @Test
    public void loadTestTokenBucketOverHttp() throws Exception {
        String clientId="load-test-http-client";
        int capacity=501;
        int numberOfRequests=501;

        rateLimitService.updateTokenBucket(clientId,(long)capacity,0L);

        String url="http://localhost:"+port+"/api/rate-limiter/check";

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody= objectMapper.writeValueAsString(new RateLimitRequest(clientId));

        HttpClient client=HttpClient.newHttpClient();
        ExecutorService executorService= Executors.newFixedThreadPool(numberOfRequests);
        CountDownLatch startLatch=new CountDownLatch(1);
        CountDownLatch doneLatch=new CountDownLatch(numberOfRequests);
        AtomicInteger allowCount=new AtomicInteger(0);
        AtomicInteger errorCount=new AtomicInteger(0);

        HttpRequest warmUp=HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type","application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        client.send(warmUp,HttpResponse.BodyHandlers.ofString());

        for(int i=0;i<numberOfRequests;i++){
            executorService.submit(() -> {
                try{
                    startLatch.await();
                    HttpRequest request= HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header("Content-Type","application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                            .build();
                    HttpResponse<String> response=client.send(request,HttpResponse.BodyHandlers.ofString());
                    if(response.statusCode()==202){
                        allowCount.incrementAndGet();
                    }
                }catch(Exception e){
                    errorCount.incrementAndGet();
                    System.out.println("Error: "+e.getClass().getSimpleName()+"->"+e.getMessage());
                }finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();

        System.out.println("Allowed: "+allowCount.get()+", Error: "+errorCount.get());
        assertTrue(allowCount.get()<=capacity,"Should less than capacity");
        executorService.shutdown();
    }
}
