package com.demo.weather_be;

import com.demo.weather_be.entity.Location;
import com.demo.weather_be.repository.LocationRepository;
import com.demo.weather_be.service.LocationService;
import com.demo.weather_be.service.WeatherApiService;
import com.demo.weather_be.service.WeatherApiService.WeatherResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class LocationServiceTests {

    private LocationRepository locationRepo;
    private WeatherApiService weatherService;
    private LocationService locationService;

    @BeforeEach
    void setUp() {
        locationRepo = mock(LocationRepository.class);
        weatherService = mock(WeatherApiService.class);
        locationService = new LocationService(locationRepo, weatherService);
    }

    @Test
    void testGetAllLocations() {
        List<Location> mockLocations = Arrays.asList(new Location("City1"), new Location("City2"));
        when(locationRepo.findAll()).thenReturn(mockLocations);

        List<Location> result = locationService.getAllLocations();
        assertEquals(2, result.size());
        verify(locationRepo, times(1)).findAll();
    }

    @Test
    void testAddLocation_Success() {
        String city = "New York";
        WeatherResult weatherResult = new WeatherResult(25.0, "Sunny");

        when(locationRepo.findByName(city)).thenReturn(Optional.empty());
        when(weatherService.fetchWeather(city)).thenReturn(weatherResult);

        Location added = locationService.addLocation(city);

        assertEquals(city, added.getName());
        assertEquals(25.0, added.getTemperature());
        assertEquals("Sunny", added.getWeatherDescription());
        assertNotNull(added.getLastUpdated());

        verify(locationRepo).save(any(Location.class));
    }

    @Test
    void testAddLocation_Duplicate() {
        String city = "Paris";
        when(locationRepo.findByName(city)).thenReturn(Optional.of(new Location(city)));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> locationService.addLocation(city));
        assertEquals("Location already exists", ex.getMessage());
        verify(locationRepo, never()).save(any());
    }

    @Test
    void testUpdateWeather_Success() {
        Long id = 1L;
        Location loc = new Location("London");
        loc.setId(id);
        WeatherResult weatherResult = new WeatherResult(18.5, "Cloudy");

        when(locationRepo.findById(id)).thenReturn(Optional.of(loc));
        when(weatherService.fetchWeather(loc.getName())).thenReturn(weatherResult);

        Location updated = locationService.updateWeather(id);

        assertEquals("London", updated.getName());
        assertEquals(18.5, updated.getTemperature());
        assertEquals("Cloudy", updated.getWeatherDescription());
        assertNotNull(updated.getLastUpdated());

        verify(locationRepo).save(loc);
    }

    @Test
    void testUpdateWeather_LocationNotFound() {
        Long id = 99L;
        when(locationRepo.findById(id)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> locationService.updateWeather(id));
        assertEquals("Location not found", ex.getMessage());
    }

    @Test
    void testDeleteLocation() {
        Long id = 2L;
        locationService.deleteLocation(id);
        verify(locationRepo, times(1)).deleteById(id);
    }
}

