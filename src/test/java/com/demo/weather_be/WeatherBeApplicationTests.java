package com.demo.weather_be;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import com.demo.weather_be.entity.Location;
import com.demo.weather_be.repository.LocationRepository;
import com.demo.weather_be.service.WeatherApiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Optional;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import org.springframework.http.MediaType;

@SpringBootTest
@AutoConfigureMockMvc
@Import(WeatherBeApplicationTests.MockConfig.class)
public class WeatherBeApplicationTests {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private LocationRepository locationRepo;

        @Autowired
        private WeatherApiService weatherService;

        @Autowired
        private ObjectMapper objectMapper;

        @BeforeEach
        void setup() {
            locationRepo.deleteAll();
        }

        @TestConfiguration
        static class MockConfig {
            @Bean
            public WeatherApiService weatherApiService() {
                return mock(WeatherApiService.class);
            }
        }

    @Test
    void testAddLocation_Success() throws Exception {
        WeatherApiService.WeatherResult result = new WeatherApiService.WeatherResult(21.0, "Clear");
        when(weatherService.fetchWeather("Berlin")).thenReturn(result);

        String jsonBody = "{\"name\":\"Berlin\"}";

        mockMvc.perform(post("/api/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Berlin"))
                .andExpect(jsonPath("$.temperature").value(21.0))
                .andExpect(jsonPath("$.weatherDescription").value("Clear"));
    }

    @Test
    void testAddLocation_Duplicate() throws Exception {
        Location loc = new Location("Tokyo");
        locationRepo.save(loc);

        String jsonBody = "{\"name\":\"Tokyo\"}";

        mockMvc.perform(post("/api/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Location already exists")));
    }

    @Test
    void testGetLocations() throws Exception {
        Location loc = new Location("Paris");
        loc.setTemperature(15.0);
        loc.setWeatherDescription("Foggy");
        locationRepo.save(loc);

        mockMvc.perform(get("/api/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Paris"))
                .andExpect(jsonPath("$[0].temperature").value(15.0))
                .andExpect(jsonPath("$[0].weatherDescription").value("Foggy"));
    }

    @Test
    void testUpdateWeather_Success() throws Exception {
        Location loc = new Location("Amsterdam");
        locationRepo.save(loc);

        WeatherApiService.WeatherResult result = new WeatherApiService.WeatherResult(12.5, "Rain");
        when(weatherService.fetchWeather("Amsterdam")).thenReturn(result);

        mockMvc.perform(put("/api/locations/" + loc.getId() + "/weather"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temperature").value(12.5))
                .andExpect(jsonPath("$.weatherDescription").value("Rain"));
    }

    @Test
    void testDeleteLocation() throws Exception {
        Location loc = new Location("Madrid");
        locationRepo.save(loc);

        mockMvc.perform(delete("/api/locations/" + loc.getId()))
                .andExpect(status().isNoContent());

        Optional<Location> deleted = locationRepo.findById(loc.getId());
        assertTrue(deleted.isEmpty());
    }
}
