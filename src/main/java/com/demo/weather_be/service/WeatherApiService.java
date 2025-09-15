package com.demo.weather_be.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;

@Service
public class WeatherApiService {

    private final RestTemplate rest = new RestTemplate();

    public WeatherResult fetchWeather(String cityName) {
        // wttr.in supports JSON format via ?format=j1
        String url = UriComponentsBuilder
                .fromUriString("https://wttr.in/" + cityName)
                .queryParam("format", "j1")
                .build()
                .toUriString();
        JsonNode root = rest.getForObject(url, JsonNode.class);
        if (root == null) {
            throw new RuntimeException("wttr.in returned no data");
        }
        JsonNode currentCondArr = root.path("current_condition");
        if (!currentCondArr.isArray() || currentCondArr.size() == 0) {
            throw new RuntimeException("wttr.in missing current condition");
        }
        JsonNode current = currentCondArr.get(0);
        // temp_C is string in JSON
        String tempStr = current.path("temp_C").asText();
        Double temp = null;
        try {
            temp = Double.valueOf(tempStr);
        } catch (NumberFormatException e) {
            temp = null;
        }
        String desc = "Unknown";
        JsonNode descArr = current.path("weatherDesc");
        if (descArr.isArray() && descArr.size() > 0) {
            desc = descArr.get(0).path("value").asText();
        }
        return new WeatherResult(temp, desc);
    }

    public static class WeatherResult {
        public Double temperature;
        public String description;
        public WeatherResult(Double temperature, String description) {
            this.temperature = temperature;
            this.description = description;
        }
    }
}
