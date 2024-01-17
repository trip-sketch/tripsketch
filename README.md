# 🚀 Tripsketch Backend Project

<div align="center">
  <a href="https://refbook.kro.kr">
    <img src="https://github.com/trip-sketch/.github/assets/51044545/ac005d05-0968-4a77-9874-8e026f36254e" alt="Refresh Bookstore Logo" width="400">
  </a>

  <br>
  <span style="font-size: 20px; color: green; background-color: yellow; padding: 10px; border-radius: 5px; text-decoration: none; border: 2px solid black; display: inline-block; margin-top: 20px;">
    "We are flying to 'sketch' a great trip in your mind." :airplane: 
  </span>
  <br>
  <a href="https://m.onestore.co.kr/mobilepoc/apps/appsDetail.omp?prodId=0000771698" style="font-size: 16px; color: blue; text-decoration: none; margin-top: 10px;">
    Download :arrow_down: 
  </a>
</div>

<div align="center">


## Tech Stack
![Language and Frameworks](https://img.shields.io/badge/-Programming%20Languages%20and%20Frameworks-8A2BE2?style=for-the-badge&logo=appveyor&logoColor=white)<br>
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Data MongoDB](https://img.shields.io/badge/Spring_Data_MongoDB-47A248?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-data-mongodb)
[![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white&labelColor=FF4081)](https://kotlinlang.org/)

<hr>

![Database and ORM](https://img.shields.io/badge/-Database%20and%20ORM-FF4500?style=for-the-badge&logo=mongodb&logoColor=white)<br>
[![MongoDB](https://img.shields.io/badge/MongoDB-47A248?style=for-the-badge&logo=mongodb&logoColor=white)](https://www.mongodb.com/)
[![Spring Data MongoDB](https://img.shields.io/badge/Spring_Data_MongoDB-47A248?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-data-mongodb)

<hr>

![Infrastructure and Deployment](https://img.shields.io/badge/-Infrastructure%20and%20Deployment-1E90FF?style=for-the-badge&logo=azure-devops&logoColor=white)<br>
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![Docker Compose](https://img.shields.io/badge/Docker_Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://docs.docker.com/compose/)
[![Oracle Cloud Infrastructure](https://img.shields.io/badge/Oracle_Cloud_Infrastructure-F80000?style=for-the-badge&logo=oracle&logoColor=white)](https://www.oracle.com/cloud/)
[![Jib](https://img.shields.io/badge/Jib-4285F4?style=for-the-badge&logo=google-container-optimized-os&logoColor=white)](https://github.com/GoogleContainerTools/jib)
[![Caddy](https://img.shields.io/badge/Caddy-00ADD8?style=for-the-badge&logo=caddy&logoColor=white)](https://caddyserver.com/)

<hr>

![Other Tools](https://img.shields.io/badge/-Other%20Tools-32CD32?style=for-the-badge&logo=nuget&logoColor=white)<br>
[![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=white)](https://swagger.io/)
[![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)](https://github.com/features/actions)
[![highlight.io](https://img.shields.io/badge/highlight.io-9F9F9F?style=for-the-badge&logo=highlightdotio&logoColor=white)](https://highlight.io/)

<hr>

![트립스케치](https://github.com/seoyeon-00/tripsketch/assets/110542210/bb42bf65-9123-47aa-8f33-d1bd115f343d)

<hr>

<div align="left">

## 📜 Introduction

We power the "TRIPSKETCH" mobile app with our robust web server application.

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
<details>
<summary><b>View File Structure</b></summary>

```
├── HELP.md
├── LICENSE
├── README.md
├── build.gradle.kts
├── docker
│   └── local.dockerfile
├── docker-compose.yml
├── gradle
│   └── wrapper
├── gradlew
├── gradlew.bat
├── output.txt
├── settings.gradle.kts
└── src
    └── main
        ├── kotlin
        │   └── kr
        │       └── kro
        │           ├── Application.kt
        │           └── tripsketch
        │               ├── auth
        │               ├── comment
        │               ├── commons
        │               ├── follow
        │               ├── notification
        │               ├── trip
        │               └── user
        └── resources
            ├── application.properties
            ├── banner.txt
            ├── logback.xml
            └── static
```

</details>

## 🌟 Key Features

- 📚 **User Authentication**: User authentication using Kakao OAuth.
- ✏️ **Travel Diary CRUD**: Create, Read, Update, Delete functionality for travel diaries.
- 🔍 **Diary Exploration/Search**: Exploration and search feature for travel diaries.
- ❤️ **Diary 'Like' Feature**: 'Like' feature for travel diaries.
- 💬 **Comment CRUD**: Comment Create, Read, Update, Delete functionality.
- 👍 **Comment 'Like' Feature**: 'Like' feature for comments.
- 📝 **Reply CRUD**: Reply Create, Read, Update, Delete functionality.
- 💖 **Reply 'Like' Feature**: 'Like' feature for replies.
- 📥 **User Subscription**: User subscription feature.
- 🔔 **Push Notifications**: Push notification and notification management feature.
- 🖊️ **Profile Editing**: Profile editing feature.

