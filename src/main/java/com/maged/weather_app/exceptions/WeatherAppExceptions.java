package com.maged.weather_app.exceptions;

public class WeatherAppExceptions {

    public static class RateLimitExceededException extends RuntimeException {
        public RateLimitExceededException() {
            super("Too mant requests. Please wait and try again in 1 minute.");
        }

        public RateLimitExceededException(String message) {
            super(message);
        }
    }

    public static class FailedToFetchCityTemprature extends RuntimeException {
        public FailedToFetchCityTemprature(String message) {
            super("Weather API error: " + message);
        }
    }

    public static class WeatherApiException extends RuntimeException {

        public WeatherApiException() {
            super();
        }
        
        public WeatherApiException(String message) {
            super("Weather API error: " + message);
        }

        public WeatherApiException(String message, Throwable cause) {
            super("Weather API error: " + message, cause);
        }
    }
}