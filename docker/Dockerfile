FROM gradle:8.10.2-jdk17 AS builder
WORKDIR /workspace

# Needed to access private GitHub Packages during dependency resolution
COPY ../loan-mate/build.gradle ./build.gradle
COPY ../loan-mate/settings.gradle ./settings.gradle
COPY ../loan-mate/gradlew ./gradlew
COPY ../loan-mate/gradle ./gradle

RUN --mount=type=secret,id=github_credentials sh -c 'chmod +x gradlew && export $(grep -v "^#" /run/secrets/github_credentials | xargs) && ./gradlew dependencies --no-daemon'

COPY ../loan-mate/src ./src

RUN --mount=type=secret,id=github_credentials sh -c 'export $(grep -v "^#" /run/secrets/github_credentials | xargs) && ./gradlew clean bootJar --no-daemon'

FROM eclipse-temurin:17-jre
ENV APP_HOME=/app

RUN groupadd --system app && useradd --system -g app app

WORKDIR ${APP_HOME}

COPY --from=builder --chown=app:app /workspace/build/libs/*.jar app.jar

USER app

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
