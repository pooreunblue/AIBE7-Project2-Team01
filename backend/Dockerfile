# ── Stage 1: Build ──────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /workspace

# Gradle wrapper & build scripts 먼저 복사 (레이어 캐시 활용)
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 의존성 미리 다운로드
RUN ./gradlew dependencies --no-daemon -q || true

# 소스 복사 후 JAR 빌드 (테스트 제외)
COPY src src
RUN ./gradlew clean bootJar -x test --no-daemon

# ── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 빌드된 JAR 복사
COPY --from=builder /workspace/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
  "-Dspring.profiles.active=prod,db,auth,redis,storage,ai", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
