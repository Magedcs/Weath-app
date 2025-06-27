package com.maged.weather_app.services;

import com.maged.weather_app.models.CityListResponse;
import com.maged.weather_app.models.CityListResponse.City;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CityService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${openweather.api.cities.url}")
    private String url;

    public List<String> fetchValidCityNames() {
        CityListResponse response = restTemplate.getForObject(url, CityListResponse.class);

        return response.getCities().stream()
                .filter(c -> c.getName() != null && !c.getName().isBlank())
                .filter(c -> c.getRank() > 0)
                .map(City::getName)
                .distinct()
                .collect(Collectors.toList());
    }
}