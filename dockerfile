FROM maven:3.9.3-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests


FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/weather-app-0.0.1-SNAPSHOT.jar weather-app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "weather-app.jar"]