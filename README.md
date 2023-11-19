# 🚀 Tripsketch Backend Project

Welcome to **Tripsketch Backend Server** built with **Spring Boot**!

- [OneStore Application Download](https://m.onestore.co.kr/mobilepoc/apps/appsDetail.omp?prodId=0000771698)

![트립스케치](https://github.com/seoyeon-00/tripsketch/assets/110542210/bb42bf65-9123-47aa-8f33-d1bd115f343d)
<img width="1120" alt="화면들" src="https://github.com/sossost/portfolio./assets/110542210/f5bb03f5-4ca1-46a8-8097-054bcc2b1454">

## 📜 Introduction
We power the "TripSketch" mobile app with our robust web server application.

📅 **Development period:** July 2023 - September 2023

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
![트립스케치 구조](https://github.com/limeorange/TripSketch/assets/78308684/8507d6f8-1580-4edd-acc2-0deaf4e6386f)

## 📦 Installation Guide
### Prerequisites
- JVM 17
- Kotlin 1.9.10
- MongoDB (NoSQL)

```bash
# Step 1: Install JVM 17
sudo apt-get update
sudo apt-get install openjdk-17-jdk

# Step 2: Install Kotlin 1.9.10
curl -s https://get.sdkman.io | bash
source \"$HOME/.sdkman/bin/sdkman-init.sh\"
sdk install kotlin 1.9.10

# Step 3: Clone the repository
git clone github.com/nea04184/tripsketch

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
