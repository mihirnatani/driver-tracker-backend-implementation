package com.drivertracker.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    @Value("${ratelimit.driver.updates-per-second}")
    private int driverUpdatesPerSecond;

    @Value("${ratelimit.global.requests-per-minute}")
    private int globalRequestsPerMinute;

    // One bucket per driverId
    private final ConcurrentHashMap<String, Bucket> driverBuckets
            = new ConcurrentHashMap<>();

    // One bucket per IP address
    private final ConcurrentHashMap<String, Bucket> ipBuckets
            = new ConcurrentHashMap<>();

    // Check if driver is allowed to send a location update
    public boolean isDriverAllowed(String driverId) {
        Bucket bucket = driverBuckets.computeIfAbsent(driverId, this::createDriverBucket);
        return bucket.tryConsume(1);
    }

    // Check if an IP is allowed to make a request
    public boolean isIpAllowed(String ipAddress) {
        Bucket bucket = ipBuckets.computeIfAbsent(ipAddress, this::createIpBucket);
        return bucket.tryConsume(1);
    }

    // Driver bucket: N updates per second
    private Bucket createDriverBucket(String driverId) {
        Bandwidth limit = Bandwidth.classic(
                driverUpdatesPerSecond,
                Refill.greedy(driverUpdatesPerSecond, Duration.ofSeconds(1))
        );
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    // IP bucket: N requests per minute
    private Bucket createIpBucket(String ip) {
        Bandwidth limit = Bandwidth.classic(
                globalRequestsPerMinute,
                Refill.greedy(globalRequestsPerMinute, Duration.ofMinutes(1))
        );
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
