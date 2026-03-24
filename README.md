[README.md](https://github.com/user-attachments/files/26203280/README.md)
# Driver Tracker — Real-Time Location Tracking System Implementation

A production-ready backend for real-time driver location tracking, built with **Spring Boot**, **WebSockets**, and **Redis**. Designed for delivery or taxi applications where passengers need to watch a driver's location live — without refreshing.

---

## 📐 Architecture Overview

```
Driver App
    │
    │  POST /api/driver/location  { driverId, lat, lng }
    ▼
Spring Boot Server
    │
    ├──► Redis SET driver:latest:{driverId}      ← stores latest position
    │
    └──► Redis PUBLISH driver:location:{driverId} ← broadcasts event
                │
                ▼
         RedisSubscriber (listener)
                │
                ▼
         PassengerSessionManager
         (finds all WebSocket sessions watching this driver)
                │
                ▼
         Passenger A,  Passenger B ...  (live update pushed)
```

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.2.x |
| Real-time | Spring WebSocket |
| Pub/Sub & Cache | Redis + Spring Data Redis |
| Auth | JWT (jjwt 0.11.5) + Spring Security |
| Rate Limiting | Bucket4j (Token Bucket Algorithm) |
| Build Tool | Maven |
| Java Version | 17 or 21 |

---

## 📁 Project Structure

```
src/main/java/com/drivertracker/
│
├── config/
│   ├── WebSocketConfig.java         # WebSocket endpoint registration
│   ├── RedisConfig.java             # Redis template + Pub/Sub listener setup
│   └── SecurityConfig.java         # Spring Security + filter chain
│
├── controller/
│   ├── DriverController.java        # POST /api/driver/location
│   └── AuthController.java         # POST /api/auth/login
│
├── model/
│   ├── LocationUpdate.java          # { driverId, lat, lng, timestamp }
│   ├── User.java                    # User entity with Role enum
│   ├── AuthRequest.java             # Login request body
│   └── AuthResponse.java           # Login response with JWT token
│
├── security/
│   ├── JwtUtil.java                 # Token generation and validation
│   ├── JwtAuthFilter.java          # Per-request JWT validation filter
│   ├── RateLimitFilter.java        # IP-level rate limiting filter
│   └── CustomUserDetailsService.java # User loading for Spring Security
│
├── service/
│   ├── LocationService.java         # Store in Redis + publish event
│   ├── RedisPublisherService.java   # Publishes to Redis channel
│   ├── AuthService.java             # Login logic + token generation
│   └── RateLimiterService.java     # Per-driver + per-IP rate limiting
│
├── subscriber/
│   └── RedisSubscriber.java        # Listens to Redis, pushes to passengers
│
└── websocket/
    ├── PassengerWebSocketHandler.java  # Handles WS connect/disconnect
    └── PassengerSessionManager.java   # Tracks driverId → passenger sessions
```

---

## ⚙️ Configuration

All config lives in `src/main/resources/application.properties`:

```properties
spring.application.name=driver-tracker
server.port=8080

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# JWT
jwt.secret=your-super-secret-key-that-is-at-least-32-characters-long
jwt.expiration=86400000

# Rate Limiting
ratelimit.driver.updates-per-second=1
ratelimit.global.requests-per-minute=100
```

> ⚠️ Never commit `jwt.secret` to version control in production. Use environment variables instead.

---

## 🚀 Getting Started

### Prerequisites

- Java 17 or 21
- Maven
- Redis running locally

### Install & Start Redis

**Docker (easiest):**
```bash
docker run -d -p 6379:6379 --name redis redis:latest
```

**Mac:**
```bash
brew install redis && brew services start redis
```

**Linux / WSL:**
```bash
sudo apt install redis-server && sudo service redis-server start
```

**Verify Redis is running:**
```bash
redis-cli ping   # should return PONG
```

### Run the Application

```bash
./mvnw spring-boot:run
```

App starts at `http://localhost:8080`

---

## 📡 API Reference

### Auth

#### Login
```
POST /api/auth/login
Content-Type: application/json

{
    "username": "driver1",
    "password": "password123"
}
```

Response:
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": "driver1",
    "role": "DRIVER"
}
```

---

### Driver

#### Send Location Update
```
POST /api/driver/location
Authorization: Bearer <token>
Content-Type: application/json

{
    "driverId": "driver1",
    "latitude": 18.5204,
    "longitude": 73.8567,
    "timestamp": 0
}
```

> If `timestamp` is `0`, the server sets it automatically.

Response: `200 OK — "Location updated"`

Rate limited to **1 update per second** per driver.

#### Get Last Known Location
```
GET /api/driver/location/{driverId}
Authorization: Bearer <token>
```

Response:
```json
{
    "driverId": "driver1",
    "latitude": 18.5204,
    "longitude": 73.8567,
    "timestamp": 1711234567123
}
```

---

### Passenger — WebSocket

Passengers connect via WebSocket. The JWT token is passed as a query parameter since WebSocket connections can't send HTTP headers.

```
ws://localhost:8080/track/{driverId}?token=<passenger_jwt>
```

Example:
```
ws://localhost:8080/track/driver1?token=eyJhbGciOiJIUzI1NiJ9...
```

Once connected, the passenger receives a JSON push every time the driver sends a location update:

```json
{
    "driverId": "driver1",
    "latitude": 18.5220,
    "longitude": 73.8575,
    "timestamp": 1711234570000
}
```

Multiple passengers can watch the same driver simultaneously.

---

## 🔐 Security

| Endpoint | Protection |
|---|---|
| `POST /api/auth/login` | Public |
| `POST /api/driver/location` | JWT required + `ROLE_DRIVER` |
| `GET /api/driver/location/{id}` | JWT required |
| `ws://.../track/{driverId}` | JWT in query param + `ROLE_PASSENGER` |

Tokens are stateless (no server-side sessions). Every request is authenticated purely by its JWT signature.

---

## 🛡️ Rate Limiting

Two levels of protection using the **Token Bucket algorithm**:

| Level | Limit | Scope |
|---|---|---|
| IP-level | 60 requests / minute | All endpoints |
| Driver-level | 1 location update / second | POST /api/driver/location |

When exceeded, the server returns:
```
HTTP 429 Too Many Requests
{"error": "Too many requests. Please slow down."}
```

---

## 🧪 Testing

### Test Users (pre-loaded in-memory)

| Username | Password | Role |
|---|---|---|
| `driver1` | `password123` | DRIVER |
| `passenger1` | `password123` | PASSENGER |

### Quick Test Flow

1. Login as driver → get token
2. Login as passenger → get token
3. Open WebSocket client (e.g. [websocketking.com](https://websocketking.com))
4. Connect to `ws://localhost:8080/track/driver1?token=<passenger_token>`
5. POST location update with driver token from Postman
6. Watch the update appear in the WebSocket client instantly

---

## 🔄 How Real-Time Push Works

```
1. Driver sends POST /api/driver/location every ~3 seconds

2. Server stores latest location in Redis:
   KEY:   driver:latest:driver1
   VALUE: {"driverId":"driver1","latitude":18.52,...}

3. Server publishes to Redis channel:
   CHANNEL: driver:location:driver1
   MESSAGE: {"driverId":"driver1","latitude":18.52,...}

4. RedisSubscriber.onMessage() fires automatically

5. Looks up all WebSocket sessions watching driver1

6. Pushes JSON to each connected passenger session
```

This design supports **horizontal scaling** — multiple server instances share state via Redis Pub/Sub. A passenger connected to Server A will still receive updates when the driver's POST hits Server B.

---

## 🗺️ What's Coming Next

- [ ] Proper exception handling & global error responses
- [ ] Dead WebSocket session cleanup (scheduled task)
- [ ] Trip management (only push when trip is active)
- [ ] Database integration (replace in-memory users)
- [ ] Docker Compose setup
- [ ] Unit & integration tests

---

## 📝 Notes

- Passwords are stored as **BCrypt hashes** — never plain text
- Redis is used for both **ephemeral storage** (latest location) and **Pub/Sub** (event broadcasting)
- The `ConcurrentHashMap` in `PassengerSessionManager` is thread-safe for concurrent connections
- WebSocket sessions are validated on connect — invalid tokens are rejected with `CloseStatus.NOT_ACCEPTABLE`
