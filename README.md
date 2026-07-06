# FlashReel Backend

Backend API for **FlashReel**, an R&D short video social app targeting the Vietnamese market.

This repository currently contains the Spring Boot backend for the MVP loop:

`publish video -> review in admin backend -> approved video appears in feed -> users view / like / follow`

## Product scope

FlashReel is planned as:

- iOS App
- Android App
- Admin backend web

The first release focuses on a tight MVP:

- account registration and login
- Google / Apple sign-in support
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

- shared defaults live in [src/main/resources/application.yaml](C:/Users/phong.truong/IdeaProjects/short-videos-appx/src/main/resources/application.yaml:1)
- local secrets live in [src/main/resources/application-local.yaml](C:/Users/phong.truong/IdeaProjects/short-videos-appx/src/main/resources/application-local.yaml:1)
- local-only config files are ignored by [.gitignore](C:/Users/phong.truong/IdeaProjects/short-videos-appx/.gitignore:1)

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
