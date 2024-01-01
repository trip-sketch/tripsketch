# JDK 이미지 사용
FROM azul/zulu-openjdk-alpine:21

WORKDIR /app

# 소스 코드 및 Gradle 스크립트 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY src src

# 애플리케이션 실행
CMD ["./gradlew", "bootRun"]
