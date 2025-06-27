package com.maged.weather_app.models;

public class CityTemperatureResult {
    private final String resultLine;
    private final boolean isWeatherApiFailure;

    public CityTemperatureResult(String resultLine, boolean isWeatherApiFailure) {
        this.resultLine = resultLine;
        this.isWeatherApiFailure = isWeatherApiFailure;
    }

    public String getResultLine() {
        return resultLine;
    }

    public boolean isWeatherApiFailure() {
        return isWeatherApiFailure;
    }
}
