package com.drivertracker.controller;

import com.drivertracker.model.LocationUpdate;
import com.drivertracker.service.LocationService;
import com.drivertracker.service.RateLimiterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/driver")
public class DriverController {

    private final LocationService locationService;
    private final RateLimiterService rateLimiterService;

    public DriverController(LocationService locationService, RateLimiterService rateLimiterService) {
        this.locationService = locationService;
        this.rateLimiterService = rateLimiterService;
    }

    @PostMapping("/location")
    public ResponseEntity<String> updateLocation(@RequestBody LocationUpdate update) {

        // Set timestamp if driver didn't send one
        if (!rateLimiterService.isDriverAllowed(update.getDriverId())) {
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Location update rate limit exceeded. Max 1 update per second.");
        }

        if (update.getTimestamp() == 0) {
            update.setTimestamp(System.currentTimeMillis());
        }

        locationService.updateLocation(update);
        return ResponseEntity.ok("Location updated");
    }

    @GetMapping("/location/{driverId}")
    public ResponseEntity<String> getLocation(@PathVariable String driverId) {
        String location = locationService.getLatestLocation(driverId);
        if (location == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(location);
    }
}