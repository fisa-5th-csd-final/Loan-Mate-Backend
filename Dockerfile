FROM gradle:8.10.2-jdk17 AS builder
WORKDIR /workspace

# Needed to access private GitHub Packages during dependency resolution
ARG GITHUB_USERNAME
ARG GITHUB_TOKEN
ENV GITHUB_USERNAME=${GITHUB_USERNAME}
ENV GITHUB_TOKEN=${GITHUB_TOKEN}

COPY loan-mate/gradlew ./gradlew
COPY loan-mate/gradle ./gradle
COPY loan-mate/build.gradle ./build.gradle
COPY loan-mate/settings.gradle ./settings.gradle
COPY loan-mate/sonar-project.properties ./sonar-project.properties
COPY loan-mate/src ./src

RUN chmod +x gradlew && ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:17-jre
ENV APP_HOME=/app
WORKDIR ${APP_HOME}

COPY --from=builder /workspace/build/libs/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
