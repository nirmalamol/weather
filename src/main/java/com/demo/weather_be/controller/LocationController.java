package com.demo.weather_be.controller;


import com.demo.weather_be.entity.Location;
import com.demo.weather_be.service.LocationService;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
//@CrossOrigin(origins = "http://localhost:3000")  // allow React frontend
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    public List<Location> getLocations() {
        return locationService.getAllLocations();
    }

    public static class CreateRequest {
        @NotBlank
        public String name;
    }

    @PostMapping
    public ResponseEntity<?> addLocation(@Valid @RequestBody CreateRequest request) {
        try {
            Location loc = locationService.addLocation(request.name.trim());
            return ResponseEntity.ok(loc);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/weather")
    public ResponseEntity<?> updateWeather(@PathVariable Long id) {
        try {
            Location loc = locationService.updateWeather(id);
            return ResponseEntity.ok(loc);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLocation(@PathVariable Long id) {
        locationService.deleteLocation(id);
        return ResponseEntity.noContent().build();
    }
}
