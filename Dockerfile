FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Gradle Wrapper 복사 및 권한 설정
COPY gradlew ./
COPY gradle ./gradle
RUN chmod +x gradlew

# 의존성 파일만 먼저 복사하여 의존성 캐싱
COPY build.gradle settings.gradle ./
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies --no-daemon \
    -Dorg.gradle.internal.http.socketTimeout=120000 \
    -Dorg.gradle.internal.http.connectionTimeout=120000

# 소스 코드 복사 및 빌드
COPY src ./src
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew build -x test --no-daemon \
    -Dorg.gradle.internal.http.socketTimeout=120000 \
    -Dorg.gradle.internal.http.connectionTimeout=120000

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
