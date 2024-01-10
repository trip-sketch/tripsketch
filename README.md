# 🚀 Tripsketch Backend Project

Welcome to **Tripsketch Backend Server** built with **Spring Boot**!

- [OneStore Application Download](https://m.onestore.co.kr/mobilepoc/apps/appsDetail.omp?prodId=0000771698)

![트립스케치](https://github.com/seoyeon-00/tripsketch/assets/110542210/bb42bf65-9123-47aa-8f33-d1bd115f343d)
<img width="1120" alt="화면들" src="https://github.com/sossost/portfolio./assets/110542210/f5bb03f5-4ca1-46a8-8097-054bcc2b1454">

## 📜 Introduction

We power the "TRIPSKETCH" mobile app with our robust web server application.

📅 **Development period:** July 2023 - September 2023

## 🛠️ Tech Stack
[![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white&labelColor=FF4081)](https://kotlinlang.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Data MongoDB](https://img.shields.io/badge/Spring_Data_MongoDB-47A248?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-data-mongodb)
[![MongoDB](https://img.shields.io/badge/MongoDB-47A248?style=for-the-badge&logo=mongodb&logoColor=white)](https://www.mongodb.com/)
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![Docker Compose](https://img.shields.io/badge/Docker_Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://docs.docker.com/compose/)
[![Jib](https://img.shields.io/badge/Jib-4285F4?style=for-the-badge&logo=google&logoColor=white)](https://github.com/GoogleContainerTools/jib)
[![Caddy](https://img.shields.io/badge/Caddy-00ADD8?style=for-the-badge&logo=caddy&logoColor=white)](https://caddyserver.com/)
[![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=white)](https://swagger.io/)
[![Oracle Cloud Infrastructure](https://img.shields.io/badge/Oracle_Cloud_Infrastructure-F80000?style=for-the-badge&logo=oracle&logoColor=white)](https://www.oracle.com/cloud/)
[![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)](https://github.com/features/actions)
[![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)](https://gradle.org/)


## 🤝 Team Members & Responsibilities

- **Hojun Song**

  - Team Leadership
  - User management
  - Image Processing
  - User Authentication
  - Managing Notifications

- **Saejin Park**

  - Implementing Search Features
  - Posts/Articles Management

- **ByeonUk Ko**

  - Category Management
  - Threaded Comments Oversight and Management

- **Hyejin Youn**
  - Image Processing

## 🛠 Architecture Diagram
![image](https://github.com/trip-sketch/tripsketch/assets/51044545/799eccc7-6aa1-46fc-9fc1-e41cfe20cc24)




## 📦 Installation Guide

### Prerequisites

- JVM 21
- Kotlin 1.9.22
- MongoDB (NoSQL)

```bash
# Step 1: Install JVM 21 and Kotlin
# If you are a Linux user, it is highly recommended to install via the Azul zulu deb or rpm package manager

# Step 2: Clone the repository
git clone github.com/trip-sketch/tripsketch

# Step 4: Navigate to the project directory
cd tripsketch

# Step 5: Build and run the project
./gradlew build

# Step 6: Run the project
./gradlew run
```

## 📂 Project File Structure

```
├── HELP.md
├── build.gradle.kts
├── gradlew
├── gradlew.bat
├── output.txt
├── settings.gradle.kts
└── src
    ├── main
    │   ├── kotlin
    │   │   └── kr
    │   │       └── kro
    │   │           ├── Application.kt
    │   │           └── tripsketch
    │   │               ├── config
    │   │               │   ├── KakaoOAuthConfig.kt
    │   │               │   ├── S3Config.kt
    │   │               │   ├── ServletFilterConfig.kt
    │   │               │   └── WebMvcConfig.kt
    │   │               ├── controllers
    │   │               │   ├── CommentController.kt
    │   │               │   ├── FollowController.kt
    │   │               │   ├── NotificationController.kt
    │   │               │   ├── OauthController.kt
    │   │               │   ├── TripController.kt
    │   │               │   └── UserController.kt
    │   │               ├── domain
    │   │               │   ├── Comment.kt
    │   │               │   ├── Follow.kt
    │   │               │   ├── Notification.kt
    │   │               │   ├── Trip.kt
    │   │               │   └── User.kt
    │   │               ├── dto
    │   │               │   ├── CommentChildrenCreateDto.kt
    │   │               │   ├── CommentCreateDto.kt
    │   │               │   ├── CommentDto.kt
    │   │               │   ├── CommentUpdateDto.kt
    │   │               │   ├── FollowDto.kt
    │   │               │   ├── KakaoRefreshRequest.kt
    │   │               │   ├── ProfileDto.kt
    │   │               │   ├── ResponseFormat.kt
    │   │               │   ├── TokenResponse.kt
    │   │               │   ├── TripAndCommentResponseDto.kt
    │   │               │   ├── TripCardDto.kt
    │   │               │   ├── TripCountryFrequencyDto.kt
    │   │               │   ├── TripCreateDto.kt
    │   │               │   ├── TripDto.kt
    │   │               │   ├── TripIdDto.kt
    │   │               │   ├── TripUpdateDto.kt
    │   │               │   ├── TripUpdateResponseDto.kt
    │   │               │   ├── UserDto.kt
    │   │               │   ├── UserProfileDto.kt
    │   │               │   └── UserUpdateDto.kt
    │   │               ├── exceptions
    │   │               │   ├── CustomException.kt
    │   │               │   └── GlobalExceptionHandler.kt
    │   │               ├── repositories
    │   │               │   ├── CommentRepository.kt
    │   │               │   ├── FollowRepository.kt
    │   │               │   ├── NotificationRepository.kt
    │   │               │   ├── TripRepository.kt
    │   │               │   └── UserRepository.kt
    │   │               ├── services
    │   │               │   ├── AuthService.kt
    │   │               │   ├── CommentService.kt
    │   │               │   ├── FollowService.kt
    │   │               │   ├── ImageService.kt
    │   │               │   ├── JwtService.kt
    │   │               │   ├── KakaoOAuthService.kt
    │   │               │   ├── NickNameService.kt
    │   │               │   ├── NotificationService.kt
    │   │               │   ├── S3Service.kt
    │   │               │   ├── TripService.kt
    │   │               │   └── UserService.kt
    │   │               └── utils
    │   │                   ├── Dotenv.kt
    │   │                   ├── JwtTokenInterceptor.kt
    │   │                   ├── PagenationUtil.kt
    │   │                   └── SimpleLoggingFilter.kt
    │   └── resources
    │       ├── META-INF
    │       ├── application.properties
    │       ├── banner.txt
    │       ├── log4j2.xml
    │       └── static
    │           └── index.html
    └── test
        └── kotlin
```

## 🎨 Project Description

### Key Features

- User authentication using Kakao OAuth
- Travel diary CRUD (Create, Read, Update, Delete) functionality
- Exploration and search feature for travel diaries
- 'Like' feature for travel diaries
- Comment CRUD functionality
- 'Like' feature for comments
- Reply CRUD functionality
- 'Like' feature for replies
- User subscription feature
- Push notification and notification management feature
- Profile editing feature
- User subscription feature
- Push notification and notification management feature
- Profile editing feature
