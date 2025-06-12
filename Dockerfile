FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/telegram-bot-0.1.0.jar app.jar
RUN groupadd -r botuser && useradd -r -g botuser botuser
RUN mkdir -p /app/data && chown -R botuser:botuser /app
USER botuser
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
