# Social Media Dashboard

Instagram-inspired full-stack starter project.

## Requirements
- Java 17+
- Maven 3.9+
- Node.js 20+
- MySQL 8+

## 1. Create database
Run in MySQL:
```sql
CREATE DATABASE social_media_dashboard;
```

## 2. Backend configuration
Open `backend/src/main/resources/application.properties`.
Change the MySQL username/password to your local MySQL credentials.

From the `backend` folder:
```bash
mvn spring-boot:run
```
Backend: http://localhost:8080

## 3. Frontend configuration
From the `frontend` folder:
```bash
npm install
npm run dev
```
Frontend: http://localhost:5173

## Demo flow
1. Register a new user.
2. Log in.
3. Create a post using text and an optional image URL.
4. Like/unlike posts.
5. Delete your own posts.

## API endpoints
- POST `/api/auth/register`
- POST `/api/auth/login`
- GET `/api/posts`
- POST `/api/posts`
- DELETE `/api/posts/{id}`
- POST `/api/posts/{id}/like`
- GET `/api/users/me`

JWT is sent as `Authorization: Bearer <token>`.

## Notes
- This is an educational intermediate project, not production-hardened.
- Images are stored as URLs for this starter version. Add multipart upload/cloud storage as a later enhancement.
- The starter has registration, login, feed, post creation/deletion, likes and profile basics. Comments, follow relationships, notifications and advanced analytics are suggested next modules.
"# SelfLearning3-1" 
"# SelfLearning3-1" 
