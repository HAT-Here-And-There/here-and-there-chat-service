FROM openjdk:21-jdk-slim as builder

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY src src
RUN chmod +x ./gradlew
RUN ./gradlew bootJar

FROM openjdk:21-jdk-slim

WORKDIR /app

COPY --from=builder build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT java -jar app.jar
