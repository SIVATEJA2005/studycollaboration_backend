# StudyCollab — Backend

This is the backend for StudyCollab, a platform I built for students to study together in real-time. It handles everything — auth, rooms, chat, file uploads, AI features, tasks, and more.

Built with Spring Boot.

---

## What this does

- Users can register, login, and activate their account via email
- Create study rooms with a name, icon, and category
- Rooms generate an invite code — share it with friends to join
- Real-time chat inside each room using WebSockets
- Upload PDFs to a room — the AI can then answer questions about them, summarize them, or generate a quiz
- Share links with auto-fetched previews
- Collaborative to-do list per room
- Topic tracker — members can claim topics and mark them as in progress or done
- Shared Pomodoro timer per room

---

## Tech I used

- Spring Boot 3
- Spring Security + JWT
- WebSocket with STOMP and SockJS
- MySQL + JPA
- Cloudinary for file storage
- Pinecone for vector storage (AI features)
- Lombok
- Maven

---

## Project structure

```
controllers/     — all REST and WebSocket controllers
services/        — business logic interfaces
serviceImpl/     — actual implementations
entities/        — database models
dto/             — request and response objects
repository/      — JPA repositories
Security/        — JWT config and filters
```

---

## Running locally

**You'll need:**
- Java 17+
- Maven
- MySQL
- A Cloudinary account
- A Pinecone account

**Steps:**

```bash
git clone https://github.com/SIVATEJA2005/studycollaboration_backend.git
cd studycollaboration_backend
```

Create `src/main/resources/application.properties` and fill in:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/studycollab
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password
spring.jpa.hibernate.ddl-auto=update

jwt.secret=your_secret_key
jwt.expiration=86400000

cloudinary.cloud-name=your_cloud_name
cloudinary.api-key=your_api_key
cloudinary.api-secret=your_api_secret

pinecone.api-key=your_pinecone_key
pinecone.index-name=your_index_name

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email
spring.mail.password=your_app_password
```

Then run:

```bash
mvn spring-boot:run
```

Server starts at `http://localhost:8080`

---

## API endpoints

**Auth**
```
POST   /api/users/register
POST   /api/users/login
GET    /api/users/activate?token=
PUT    /api/users/profile
```

**Rooms**
```
POST   /room/create
GET    /room/myRooms
GET    /room/{id}
POST   /room/join-by-code
GET    /room/leave/{id}
```

**AI**
```
POST   /api/ai/index-pdf
POST   /api/ai/ask
POST   /api/ai/summarize
POST   /api/ai/quiz
DELETE /api/ai/index/{resourceId}
```

**Resources**
```
POST   /api/resources/upload/{roomId}
POST   /api/resources/link/{roomId}
GET    /api/resources/room/{roomId}
DELETE /api/resources/{resourceId}
```

**Tasks**
```
POST   /api/todo/create/{roomId}
GET    /api/todo/room/{roomId}
PUT    /api/todo/toggle/{todoId}
DELETE /api/todo/delete/{todoId}
```

**Topics**
```
POST   /api/topics/room/{roomId}
GET    /api/topics/room/{roomId}
PUT    /api/topics/{topicId}/status
PUT    /api/topics/{topicId}/claim
DELETE /api/topics/{topicId}
```

**Pomodoro**
```
POST   /api/pomodoro/room/{roomId}/start
PUT    /api/pomodoro/{sessionId}/toggle
PUT    /api/pomodoro/{sessionId}/finish
```

**WebSocket**
```
Connect:    /ws  (SockJS)
Send to:    /app/chat/{roomId}
Listen on:  /topic/room{roomId}
```

---

## Notes

- All protected endpoints require `Authorization: Bearer <token>` header
- PDFs are stored on Cloudinary, indexed into Pinecone for the AI features
- WebSocket auth uses the same JWT token

---

## GitHub

[github.com/SIVATEJA2005/studycollaboration_backend](https://github.com/SIVATEJA2005/studycollaboration_backend)
