package com.maged.weather_app.services;

import com.maged.weather_app.exceptions.WeatherAppExceptions.*;
import com.maged.weather_app.models.WeatherResponse;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.time.Duration;
@Service
public class WeatherService {

    @Value("${openweather.api.key}")
    private String apiKey;

    @Value("${openweather.api.url}")
    private String apiUrl;

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

                if(response == null || response.getMain() == null) 
                     throw new FailedToFetchCityTemprature("Failed to fetch temperature for city: " + city);

                return response.getMain().getTemp();
            } catch (Exception e) {
                throw new WeatherApiException();
            }
        } else {
            throw new RateLimitExceededException();
        }
    }

    @Recover
    public Double recoverFromWeatherApiException(WeatherApiException ex, String city) {
        throw new WeatherApiException("something went wrong in our side, please try again later.");
    }
}