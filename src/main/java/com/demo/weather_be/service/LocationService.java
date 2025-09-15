package com.demo.weather_be.service;


import com.demo.weather_be.entity.Location;
import com.demo.weather_be.repository.LocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LocationService {

    private final LocationRepository locationRepo;
    private final WeatherApiService weatherService;

    public LocationService(LocationRepository locationRepo,
                           WeatherApiService weatherService) {
        this.locationRepo = locationRepo;
        this.weatherService = weatherService;
    }

    public List<Location> getAllLocations() {
        return locationRepo.findAll();
    }

    @Transactional
    public Location addLocation(String cityName) {
        // check duplicate
        if (locationRepo.findByName(cityName).isPresent()) {
            throw new RuntimeException("Location already exists");
        }
        Location loc = new Location(cityName);
        // fetch weather now
        WeatherApiService.WeatherResult wr = weatherService.fetchWeather(cityName);
        loc.setTemperature(wr.temperature);
        loc.setWeatherDescription(wr.description);
        loc.setLastUpdated(LocalDateTime.now());
        locationRepo.save(loc);
        return loc;
    }

    @Transactional
    public Location updateWeather(Long id) {
        Location loc = locationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found"));
        WeatherApiService.WeatherResult wr = weatherService.fetchWeather(loc.getName());
        loc.setTemperature(wr.temperature);
        loc.setWeatherDescription(wr.description);
        loc.setLastUpdated(LocalDateTime.now());
        locationRepo.save(loc);
        return loc;
    }

    @Transactional
    public void deleteLocation(Long id) {
        locationRepo.deleteById(id);
    }
}
