package com.drivertracker.service;

import com.drivertracker.model.LocationUpdate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LocationService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisPublisherService publisherService;
    private final ObjectMapper objectMapper;

    public LocationService(RedisTemplate<String, String> redisTemplate,
                           RedisPublisherService publisherService,
                           ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.publisherService = publisherService;
        this.objectMapper = objectMapper;
    }

    public void updateLocation(LocationUpdate update) {
        try {
            // 1. Convert location object to JSON string
            String locationJson = objectMapper.writeValueAsString(update);

            // 2. Store latest location in Redis
            String key = "driver:latest:" + update.getDriverId();
            redisTemplate.opsForValue().set(key, locationJson);

            // 3. Publish to Redis channel so subscribers get notified
            publisherService.publish(update.getDriverId(), locationJson);

        } catch (Exception e) {
            System.out.println("Error updating location: " + e.getMessage());
        }
    }

    // Optional: fetch last known location of a driver
    public String getLatestLocation(String driverId) {
        return redisTemplate.opsForValue().get("driver:latest:" + driverId);
    }
}