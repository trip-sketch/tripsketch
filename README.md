# Tripsketch Backend Project

#### Tripsektch Backend Server with Spring Boot

<br/>

![트립스케치](https://github.com/seoyeon-00/tripsketch/assets/110542210/bb42bf65-9123-47aa-8f33-d1bd115f343d)

# Project Introduction
The web server application of the mobile application "TripSketch"

## Development period
07.2023 ~ 09.2023

### Member
Hojun Song, Park Sejin, Ko Byungwook, Yoon Hyejin

### Technologies Used

- Kotlin
- Spring Boot 
- MongoDB


#### Installation Guide

### Prerequisites
- JVM 17
- Kotlin 1.9.10

Follow the steps below to set up and run the project:

# Step 1: Install JVM 17
sudo apt-get update
sudo apt-get install openjdk-17-jdk

# Step 2: Install Kotlin 1.9.10
curl -s https://get.sdkman.io | bash  # Install SDKMAN if not already installed
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install kotlin 1.9.10

# Step 3: Clone the repository (Assuming it's on GitHub, adjust for other VCS if needed)
git clone github.com/nea04184/tripsketch

# Step 4: Navigate to the project directory
cd tripsketch

# Step 5: Build and run the project (Adjust this step as per your project's build tool e.g. Gradle, Maven, etc.)
./gradlew build  

# Step 6: Run the project (This might differ based on your project's structure)
./gradlew run


#### Project File Structure

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
    │   │               │   ├── TripLikeController.kt
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
    │   │               │   ├── TripLikeService.kt
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

## Project Description

### Key Features

- User authentication using Kakao OAuth
- Travel diary CRUD (Create, Read, Update, Delete) functionality
- Exploration and search feature for travel diaries
- 'Like' feature for travel diaries
- Comment CRUD functionality
- 'Like' feature for comments
- User subscription feature
- Push notification and notification management feature
- Profile editing feature
