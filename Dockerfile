# Stage 1: Build stage
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

COPY gradlew .
COPY gradle gradle

COPY build.gradle settings.gradle gradle.properties* ./

RUN ./gradlew --no-daemon dependencies

COPY src src
RUN ./gradlew --no-daemon bootJar -x test


# Stage 2: Runtime stage
FROM amazoncorretto:21-alpine
WORKDIR /app

ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS} -jar /app/app.jar"]