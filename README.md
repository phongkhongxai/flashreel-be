# FlashReel Backend

[![Backend CI](https://github.com/phongkhongxai/flashreel-be/actions/workflows/ci.yml/badge.svg)](https://github.com/phongkhongxai/flashreel-be/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring%20Boot](https://img.shields.io/badge/Spring%20Boot-3.5.16-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)

Backend API for **FlashReel**, an R&D short video social app targeting the Vietnamese market.

This repository currently contains the Spring Boot backend for the MVP loop:

`publish video -> review in admin backend -> approved video appears in feed -> users view / like / follow`

## Repositories

| Component | Repository |
|-----------|------------|
| 🖥 Backend | https://github.com/phongkhongxai/flashreel-be |
| 🌐 Frontend | https://github.com/phongkhongxai/flashreel-fe |
| 📱 Android app | https://github.com/phongkhongxai/flashreel-android |

## Product scope

FlashReel is planned as:

- Android App
- Admin backend web

The first release focuses on a tight MVP:

- account registration and login
- Google sign-in support
- profile management
- short video upload
- moderation workflow
- popularity-based feed
- likes and follows
- multilingual support for app and admin surfaces

This repository is the backend service supporting those flows.

## Current backend capabilities

Based on the code in this repo, the backend already includes these main areas:

- authentication with JWT, refresh, logout, token introspection
- email verification flow
- outbound authentication integration
- user profile APIs
- account cancellation flow
- video upload and resubmission
- admin review APIs: pending / approved / rejected / take-down
- public feed, latest feed, playback detail, view counting
- like / unlike
- follow / unfollow creator
- comment APIs
- file storage integration with MinIO
- MySQL persistence
- Redis dependency
- OpenAPI / Swagger UI

## Tech stack

- Java 21
- Spring Boot 3
- Spring Web
- Spring Security OAuth2 Resource Server
- Spring Data JPA
- Spring Data Redis
- MySQL
- MinIO
- Spring Mail
- SpringDoc OpenAPI
- Maven

## Business rules and MVP constraints

- supported video formats: `MP4`, `MOV`
- video duration: `5 seconds` to `3 minutes`
- max video size: `300MB`
- default cover: first frame of the video
- feed ranking: likes + views + time decay
- rejected videos can be resubmitted as new submissions
- initial target market: Vietnam
- planned languages:
  - app: Vietnamese, English, Traditional Chinese
  - admin: Vietnamese, English, Chinese

## Local setup

### Prerequisites

- JDK 21
- Maven Wrapper in repo (`mvnw.cmd`)
- MySQL
- Redis
- MinIO

### Configuration

The project is configured so secrets do not need to stay in `application.yaml`.

- shared defaults live in [src/main/resources/application.yaml].
- local secrets live in [src/main/resources/application-local.yaml].
- local-only config files are ignored by [.gitignore].

Enable the local profile before starting the app:

```powershell
$env:SPRING_PROFILES_ACTIVE="local"
.\mvnw.cmd spring-boot:run
```

In IntelliJ Spring Boot run configuration, set:

- `Active profiles`: `local`

### Local infrastructure with Docker

There are two compose files in the project flow:

- `docker-compose.local.yml`: your machine-specific local file, ignored from Git
- [docker-compose.example.yml](C:/Users/phong.truong/IdeaProjects/short-videos-appx/docker-compose.example.yml:1): clean sample file for the repo

If needed, create your local compose from the example and adjust passwords, ports, and host volumes for your machine.

## Run

Start the backend:

```powershell
$env:SPRING_PROFILES_ACTIVE="local"
.\mvnw.cmd spring-boot:run
```

Build the project:

```powershell
.\mvnw.cmd clean package
```

## API areas

Main controller groups currently present:

- `/auth`
- `/users`
- `/account`
- `/videos`
- `/admin/videos`
- `/videos/{videoId}/comments`

Examples:

- `POST /auth/login`
- `POST /auth/verify-code`
- `POST /users/registeration`
- `GET /users/me/profile`
- `PUT /users/me/profile`
- `POST /videos`
- `GET /videos/feed`
- `POST /videos/{id}/like`
- `POST /users/{authorId}/follow`
- `POST /admin/videos/{id}/approve`
- `POST /admin/videos/{id}/reject`

## API documentation

SpringDoc is enabled in the project. After the app starts, check Swagger UI at one of these URLs:

- `http://localhost:8080/swagger-ui/index.html`
- `http://localhost:8080/swagger-ui.html`

## Suggested next documentation additions

- ERD for users, videos, likes, follows, comments, moderation
- role matrix for `USER`, `ADMIN`, `REVIEWER`
- environment variable reference
- moderation status lifecycle
- deployment notes for object storage, mail, and OAuth providers

## Status notes

- budget is not yet confirmed
- target go-live date is not yet confirmed
- visual style and brand assets are still to be finalized
- compliance details for the Vietnam market still need a dedicated review
