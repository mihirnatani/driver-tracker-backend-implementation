package com.drivertracker.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
public class RedisPublisherService {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisPublisherService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void publish(String driverId, String locationJson) {
        String channel = "driver:location:" + driverId;
        redisTemplate.convertAndSend(channel, locationJson);
        System.out.println("Published to channel: " + channel);
    }
}