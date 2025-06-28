package com.maged.weather_app.controllers;


import com.maged.weather_app.services.WeatherService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/weather")
public class WeatherController {

    @Autowired
    private WeatherService weatherService;


    @GetMapping("/temperature")
    public String getTemperature() {
        String defaultCity = "New York";
        Double temp = weatherService.getCityTemperature(defaultCity);
        return temp != null ? String.format("Current temperature in %s is %.2f°C", defaultCity, temp) : "Temperature data not available.";
    }

    @GetMapping("/city/temperature")
    public String getTemperatureByCity(@RequestParam("city") String city) {
        Double temp = weatherService.getCityTemperature(city);
        return temp != null
                ? String.format("Current temperature in %s is %.2f°C", city, temp)
                : String.format("Temperature data for %s not available.", city);
    }

    @GetMapping("/temperatures")
    public List<String> getAllCityTemperatures() {
        return weatherService.fetchTemperaturesForAllCities();
     }
}