# 🌤️ Weather App

A Spring Boot Java:17 application that retrieves real-time temperature data for one or more cities using the OpenWeatherMap API. It features:

- 🔁 Retry logic  
- ⏳ Rate limiting  
- ⚙️ Concurrent data fetching  
- 🐳 Dockerization  
- 🧪 Tested with Postman and browser tools  

---

## 🚀 Features

### ✅ 1. City Temperature Endpoints

- **Default city temperature (New York):**  
  ```
  GET http://localhost:8080/weather/temperature
  ```

- **Specific city temperature:**  
  ```
  GET http://localhost:8080/weather/city/temperature?city=$cityName
  ```

- **Temperatures for multiple cities from API:**  
  ```
  GET http://localhost:8080/weather/temperatures
  ```

---

## ⏳ 2. Rate Limiting (60 requests per minute)

Rate limiting is implemented using [Bucket4j](https://github.com/bucket4j/bucket4j). Since the free OpenWeatherMap tier supports up to 60 requests per minute I went for that limit.

To test the rate limiter:
- Call:
  ```
  http://localhost:8080/weather/temperatures
  ```
  multiple times in rapid succession.
- After hitting the limit, you’ll see output like:
  ```
  CityName : RateLimited
  ```

---

## 🔁 3. Retry Logic

Spring Retry is used to handle temporary API issues.

Annotated with:

```java
@Retryable(
    value = WeatherApiException.class,
    maxAttempts = 5,
    backoff = @Backoff(delay = 1000, multiplier = 2)
)
```

To test retry behavior:
- Use a faulty city name like:
  ```
  http://localhost:8080/weather/city/temperature?city=Neww Yaork
  ```
- The system will attempt 5 retries with exponential backoff (~16 seconds total) before responding with an error.

---

## ⚙️ 4. Concurrency

Concurrent execution is handled via `ExecutorService` with a fixed thread pool of 10 threads.

### 📈 Performance Comparison

| Branch | Avg Response Time |
|--------|-------------------|
| `weather-app-implementation` (no concurrency) | ~1.1s |
| `main` (with concurrency) | ~350ms |

To test:
```
GET http://localhost:8080/weather/temperatures
```

---

## 🐳 5. Docker Support

### 🧱 Build the Docker Image

```bash
docker build -t weather-app:latest .
```

### ▶️ Run the Container

```bash
docker run -p 8080:8080 weather-app:latest
```

> 💡 Dockerfile is optimized with caching. `pom.xml` is copied and dependencies downloaded **before** copying source code, so builds are fast unless dependencies change. (First image build could take about 50 seconds)

---

## 💡 Future Improvements If Had More Time

- 🧠 In-memory caching for performance and fallback values
- ☁️ Cloud database for persistent storage
- 📩 Kafka integration for event-driven alerts (critical weather conditions per se)
- 🎨 Simple modern frontend with HTML/CSS/JS


## ▶️ How to Run

### 🐳 Prerequisites

- Make sure **Docker Desktop** is installed and running on your machine.
- Java, Maven, and Postman are optional if you're using Docker only.

### 🧱 Build the Docker Image

From the project root (where the Dockerfile is located), run:

```bash
docker build -t weather-app:latest .
```

> This will download a Maven-based base image, copy dependencies and source code, and build the app.  
> The first build may take around a minute. Subsequent builds will be faster due to Docker layer caching.

### 🚀 Run the Container

```bash
docker run -p 8080:8080 weather-app:latest
```

- This starts the app inside a container and maps port `8080` to your local machine.
- Visit `http://localhost:8080` to access the endpoints.

### 🔁 Example Usage

Test the endpoints via browser or Postman:

- `http://localhost:8080/weather/temperature`
- `http://localhost:8080/weather/city/temperature?city=London`
- `http://localhost:8080/weather/temperatures`
