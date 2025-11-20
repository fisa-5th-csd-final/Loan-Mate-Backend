FROM gradle:8.10.2-jdk17 AS builder
WORKDIR /workspace

# Needed to access private GitHub Packages during dependency resolution
ARG GITHUB_USERNAME
ARG GITHUB_TOKEN
ENV GITHUB_USERNAME=${GITHUB_USERNAME}
ENV GITHUB_TOKEN=${GITHUB_TOKEN}

COPY ../loan-mate/build.gradle ./build.gradle
COPY ../loan-mate/settings.gradle ./settings.gradle
COPY ../loan-mate/gradlew ./gradlew
COPY ../loan-mate/gradle ./gradle

RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

COPY ../loan-mate/src ./src

RUN ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:17-jre
ENV APP_HOME=/app

RUN groupadd --system app && useradd --system -g app app

WORKDIR ${APP_HOME}

COPY --from=builder --chown=app:app /workspace/build/libs/*.jar app.jar

USER app

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
