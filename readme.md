# AuthService

AuthService is a Spring Boot-based microservice responsible for secure user registration, JWT-based authentication, and emitting user-related events to Apache Kafka. It works with a downstream service, [UserService](https://github.com/yashamrahs/UserService), which persists user data in a MySQL database.

---

## Features

- User Signup with JWT + Refresh Token generation
- Protected ping endpoint with JWT validation
- Kafka integration to publish user events
- Custom JWT implementation
- Token expiration and refresh logic
- Password encryption using BCrypt
- Docker-based setup with Kafka, Zookeeper, and MySQL

---

## Technology Stack

| Component        | Tech                             |
| ---------------- |----------------------------------|
| Framework        | Spring Boot 3                    |
| Security         | Spring Security, JWT             |
| Messaging        | Apache Kafka                     |
| ORM              | Spring Data JPA, Hibernate       |
| Serialization    | Jackson, Custom Kafka Serializer |
| DB               | MySQL                            |
| Build Tool       | Gradle                           |
| Containerization | Docker, Docker Compose           |

---

## Authentication Workflow

### POST `/auth/v1/signup`

- Accepts a `UserInfoDto` containing username, password, and user details
- Checks if user already exists
- If not:
  - Password is hashed using BCrypt
  - New user is persisted
  - JWT access token and refresh token are generated
  - A Kafka event (`UserInfoEvent`) is published to topic `user_service`
  - This event is consumed by [UserService](https://github.com/yashamrahs/UserService) to persist user data
- Returns access token, refresh token, and user ID
 

### GET `/auth/v1/ping`

- Requires a valid `Authorization: Bearer <JWT>` header
- Returns the authenticated user's ID if valid

### GET `/health`

- Simple health check that returns `true` with 200 OK

---

## JWT Details

- Signed using HMAC secret key
- Standard claims:
    - `sub`: userId
    - `iat`: Issued At
    - `exp`: Expiry Time
- Token is required in `Authorization` header for protected APIs
- Refresh tokens are stored and validated separately

---

## Entities and Data Storage

### `UserInfo`


| Field    | Type   | Description                 |
| -------- | ------ | --------------------------- |
| userId   | String | UUID of the user            |
| username | String | Unique username             |
| password | String | Encrypted password (BCrypt) |
| roles    | Set    | Associated user roles       |

### `UserRole`

| Field  | Type   | Description            |
| ------ | ------ | ---------------------- |
| roleId | Long   | Role identifier        |
| name   | String | Role name (e.g., USER) |

### `RefreshToken`

| Field      | Type    | Description                  |
| ---------- | ------- |------------------------------|
| id         | Int     | DB generated ID              |
| token      | String  | Refresh token UUID           |
| expiryDate | Instant | Expiration time of the token |
| userInfo   | FK      | Associated `UserInfo` entity |

---


## Kafka Integration

### Producer Configuration (application.properties)

```
spring.kafka.bootstrap-servers=192.168.1.11:9092
spring.kafka.producer.value-serializer=com.authService.serializer.UserInfoSerializer
spring.kafka.topic-json.name=user_service
```

### Custom Serializer

```java
public class UserInfoSerializer implements Serializer<UserInfoDto> {
    public byte[] serialize(String topic, UserInfoDto data) {
        return new ObjectMapper().writeValueAsString(data).getBytes();
    }
}
```

### Kafka Send Logic

```java
public void sendEventToKafka(UserInfoEvent eventData) {
    Message<UserInfoEvent> message = MessageBuilder
        .withPayload(eventData)
        .setHeader(KafkaHeaders.TOPIC, topicJsonName)
        .build();
    kafkaTemplate.send(message);
}
```

---

## API Endpoints

| Method | Endpoint          | Description                      |
| ------ | ----------------- | -------------------------------- |
| POST   | `/auth/v1/signup` | Register new user, return tokens |
| GET    | `/auth/v1/ping`   | Protected route to verify token  |
| GET    | `/health`         | Service health check             |

### Sample Register Request

```json
{
  "username": "testuser10",
  "password": "testpass10",
  "first_name": "yash",
  "last_name": "sharma",
  "phone_number": 1234567890,
  "email": "yash@gmail.com"
}
```

### Sample Response

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlcjEwIiwiaWF0IjoxNzUxMzk4ODIwLCJleHAiOjE3NTEzOTg4ODB9.uuWuPoBKDebjD_R0X6Nm4pba3bUQ4yzbkL1Qc9DMtD4",
  "token": "47ee6d87-9129-40a4-bbc0-c3689639dc0a",
  "userId": "9c3f1ecc-39cc-48bf-a040-bc1326d05b9b"
}
```

---

## Event Flow

1. User registers via `/auth/v1/signup`
2. AuthService creates the user, generates tokens, and sends Kafka event
3. [UserService](https://github.com/yashamrahs/UserService) listens to the topic `user_service`
4. UserService saves the user to its database

---

## Setup Instructions

### 1. Start Docker Services

```bash
cd docker/kafka-installation
docker-compose up -d
```

This launches:

- Kafka (port 9092)
- Zookeeper (port 2181)
- MySQL (port 3306)

### 2. Run AuthService

```bash
./gradlew bootRun
```

Or use your IDE to run `App.java`.

---

## Troubleshooting

- If the consumer isn't receiving events:
    - Verify IP in `KAFKA_ADVERTISED_LISTENERS`
    - Match producer & consumer serializers
- Kafka topic not found:
    - Ensure topic `user_service` exists
- MySQL issues:
    - Ensure container DB matches `application.properties`

---

## Author

**Yash Sharma**\
GitHub: [@yashamrahs](https://github.com/yashamrahs)


