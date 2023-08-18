import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.1.2"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.spring") version "1.9.0"
}

group = "kr.kro"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    // 스프링 기반 애플리케이션을 위한 스프링 부트 스타터
    implementation("org.springframework.boot:spring-boot-starter")

    // 코틀린 validation 을 위한 스타터
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // 코틀린 리플렉션 기능
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // 스프링 MVC를 사용한 웹 애플리케이션, RESTful 애플리케이션을 만들기 위한 스타터
    // 기본적으로 내장 컨테이너로 Tomcat을 사용합니다.
    implementation("org.springframework.boot:spring-boot-starter-web")

    // 스프링 부트 데이터 몽고디비 스타터, 스프링 기반 애플리케이션을 위한 MongoDB 사용을 간소화합니다
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")

    // 코틀린 클래스와 데이터 클래스의 직렬화/역직렬화를 지원하는 모듈
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // OpenAPI와 Swagger가 제공하는 UI를 사용하여 API 문서화를 위한 스프링 MVC와 스프링 WebFlux 통합
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.2")

    // JUnit, Hamcrest, Mockito를 포함한 라이브러리로 스프링 부트 애플리케이션 테스팅을 위한 스타터
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // 코틀린 표준 라이브러리
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    // 스프링 프레임워크의 Reactive Web 지원을 이용한 WebFlux 애플리케이션을 만들기 위한 스타터
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // JVM을 위한 Non-Blocking 반응형 기반. Flux (for [N] elements)와 Mono (for [0|1] elements)라는 컴포저블 비동기 시퀀스 API를 제공하며, Reactive Streams 사양을 광범위하게 구현
    implementation("io.projectreactor:reactor-core")

    // 코틀린의 코루틴 지원
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")

    // kotlinx.coroutines에 대한 Reactive Extensions 바인딩을 제공
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.5.2")

    // 자바 서블릿 API
    implementation("javax.servlet:javax.servlet-api:4.0.1")

    // 자바와 안드로이드를 위한 JSON 웹 토큰(JWT)
    implementation("io.jsonwebtoken:jjwt-api:0.11.2")

    // 자바 JWT 라이브러리의 구현체
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.2")

    // JWT의 직렬화/역직렬화를 지원
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.2")

    // Java에서 .env 파일을 관리하는 라이브러리
    implementation("io.github.cdimascio:java-dotenv:5.2.2")

    // logger dependencies
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.14.1")
    implementation("org.apache.logging.log4j:log4j-core:2.17.1")

    // 오라클 SDK
    implementation("com.oracle.oci.sdk:oci-java-sdk-bom:2.7.1") // 의존성+버전 관리
    implementation("com.oracle.database.jdbc:ojdbc8:19.8.0.0") // 오라클 연결+데이터 액세스
    implementation("com.oracle.oci.sdk:oci-java-sdk:3.23.0") // OCI SDK

    // for EXPO Notification
    implementation("com.squareup.okhttp3:okhttp:4.9.1")

    // JSON Parse
    implementation("org.json:json:20210307")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
