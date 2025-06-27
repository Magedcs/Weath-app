package com.maged.weather_app.services;

import com.maged.weather_app.exceptions.WeatherAppExceptions.*;
import com.maged.weather_app.models.WeatherResponse;
import io.github.bucket4j.Bucket;

import com.maged.weather_app.models.CityTemperatureResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
@Service
public class WeatherService {

    @Value("${openweather.api.key}")
    private String apiKey;

    @Value("${openweather.api.url}")
    private String apiUrl;

    @Autowired
    private CityService cityService;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final Bucket bucket = Bucket.builder()
    .addLimit(limit -> limit
        .capacity(60)
        .refillGreedy(60, Duration.ofMinutes(1)))
    .build();


    @Retryable(
        value = WeatherApiException.class,
        maxAttempts = 5,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public Double getCityTemperature(String city) {
        if (bucket.tryConsume(1)) {
            try {
                String url = String.format("%s?q=%s&appid=%s&units=metric", apiUrl, city, apiKey);
                WeatherResponse response = restTemplate.getForObject(url, WeatherResponse.class);

                if(response == null || response.getMain() == null || response.getMain().getTemp() == null) 
                     throw new FailedToFetchCityTemprature("Failed to fetch temperature for city: " + city);

                return response.getMain().getTemp();
            } catch (Exception e) {
                throw new WeatherApiException();
            }
        } else {
            throw new RateLimitExceededException();
        }
    }

    public List<String> fetchTemperaturesForAllCities() {
        List<String> cities = cityService.fetchValidCityNames();

        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<CityTemperatureResult>> futures = new ArrayList<>();

        for (String city : cities) {
            futures.add(executor.submit(() -> {
                try {
                    Double temp = getCityTemperature(city);
                    return new CityTemperatureResult(String.format("%s : %.2f°C", city, temp), false);
                } 
                catch (RateLimitExceededException e) {
                    return new CityTemperatureResult(city + " : RateLimited", false);

                } catch (FailedToFetchCityTemprature e) {
                    return new CityTemperatureResult(city + " : N/A", false);

                } catch (WeatherApiException e) {
                    return new CityTemperatureResult(city + " : WeatherApiError", true);

                } catch (Exception e) {
                    return new CityTemperatureResult(city + " : UnknownError", true);
                }
            }));
        }

        executor.shutdown();

        List<CityTemperatureResult> results = futures.stream()
            .map(future -> {
                try {
                    return future.get();
                } catch (Exception e) {
                    return new CityTemperatureResult("Na : Na", true);
                }
            })
        .collect(Collectors.toList());

        boolean hasWeatherApiFailures = results.stream().anyMatch(CityTemperatureResult::isWeatherApiFailure);

        List<String> output = results.stream()
            .map(CityTemperatureResult::getResultLine)
            .sorted((a, b) -> {
                try {
                    Double tempA = Double.parseDouble(a.split(":")[1].replace("°C", "").trim());
                    Double tempB = Double.parseDouble(b.split(":")[1].replace("°C", "").trim());
                    return Double.compare(tempB, tempA);
                } catch (Exception e) {
                    return 0;
                }
            })
            .collect(Collectors.toList());

        if (hasWeatherApiFailures) {
            output.add("Not All Cities Were Fetched");
        }

        return output;
    }
}