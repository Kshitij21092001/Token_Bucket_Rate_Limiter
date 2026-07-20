package com.kshitij.ratelimiter.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayDeque;
import java.util.Deque;

@Getter
@Setter
public class SlidingWindow {
    private Integer windowSizeSeconds;
    private Integer maxRequests;
    private Deque<Long> timeStampDeque;

    public SlidingWindow(Integer windowSizeSeconds, Integer maxRequests) {
        this.windowSizeSeconds = windowSizeSeconds;
        this.maxRequests = maxRequests;
        this.timeStampDeque = new ArrayDeque<>();
    }
}
