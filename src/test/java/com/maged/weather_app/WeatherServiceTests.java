package com.maged.weather_app;

import com.maged.weather_app.exceptions.WeatherAppExceptions.*;
import com.maged.weather_app.models.CityListResponse;
import com.maged.weather_app.models.WeatherResponse;
import com.maged.weather_app.services.CityService;
import com.maged.weather_app.services.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class WeatherServiceTests {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CityService cityService;

    @InjectMocks
    private WeatherService weatherService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(weatherService, "apiKey", "dummy-key");
        ReflectionTestUtils.setField(weatherService, "apiUrl", "http://mock-api");
    }

    @Test
    public void test_getCityTemperature_validCity() {
        WeatherResponse.Main main = new WeatherResponse.Main();
        main.setTemp(25.0);
        WeatherResponse mockResponse = new WeatherResponse();
        mockResponse.setMain(main);

        when(restTemplate.getForObject(anyString(), eq(WeatherResponse.class))).thenReturn(mockResponse);
        Double temp = weatherService.getCityTemperature("London");

        assertNotNull(temp);
        assertEquals(25.0, temp);
    }

    @Test
    public void test_getCityTemperature_weatherApiNullResponse() {
        when(restTemplate.getForObject(anyString(), eq(WeatherResponse.class))).thenReturn(null);
        assertThrows(FailedToFetchCityTemprature.class, () -> weatherService.getCityTemperature("London"));
    }

    @Test
    public void test_fetchValidCityNames_filtersInvalidAndDuplicates() {
        CityListResponse.City c1 = new CityListResponse.City(); c1.setName("London"); c1.setRank(1);
        CityListResponse.City c2 = new CityListResponse.City(); c2.setName(" "); c2.setRank(2);
        CityListResponse.City c3 = new CityListResponse.City(); c3.setName("London"); c3.setRank(1);
        CityListResponse.City c4 = new CityListResponse.City(); c4.setName(null); c4.setRank(1);

        CityListResponse mockResponse = new CityListResponse();
        mockResponse.setCities(Arrays.asList(c1, c2, c3, c4));

        CityService realService = new CityService();
        ReflectionTestUtils.setField(realService, "restTemplate", restTemplate);
        when(restTemplate.getForObject(anyString(), eq(CityListResponse.class))).thenReturn(mockResponse);
        ReflectionTestUtils.setField(realService, "url", "http://mock-cities");

        List<String> validCities = realService.fetchValidCityNames();
        assertEquals(1, validCities.size());
        assertEquals("London", validCities.get(0));
    }
}
