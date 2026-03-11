# EventHub API

Event management API built with Java 21 and Spring Boot 3.5.

## Stack

- Java 21, Spring Boot 3.5
- Spring Security + JWT
- Spring Data JPA (PostgreSQL for prod, H2 for dev)
- Spring Cache, Bean Validation, Lombok
- Docker + docker-compose

## Running

**Dev (H2):**
```bash
./mvnw spring-boot:run
```
Runs on `http://localhost:8080`. H2 console available at `/h2-console`.

**Docker (PostgreSQL):**
```bash
docker-compose up --build
```

**Tests:**
```bash
./mvnw test
```

## Endpoints

### Auth
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/auth/register` | Public | Register user |
| POST | `/api/auth/login` | Public | Login, returns JWT |

### Events
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/events` | Public | List events |
| GET | `/api/events/{id}` | Public | Get by id |
| POST | `/api/events` | ADMIN | Create event |
| PUT | `/api/events/{id}` | ADMIN | Update event |
| DELETE | `/api/events/{id}` | ADMIN | Delete event |

### Tickets
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/tickets` | USER/ADMIN | Buy ticket |
| GET | `/api/tickets/participant/{id}` | USER/ADMIN | Ticket history |

### Auth usage
```
Authorization: Bearer <token>
```
Default admin: `admin / admin123`

## Technical Decisions

**Database:** PostgreSQL in production for reliability and concurrency support. H2 in dev for quick startup without external deps. Separated via Spring profiles.

**Concurrency — Optimistic Locking:** Used `@Version` on the Event entity to prevent overbooking. If two users try to buy the last ticket at the same time, the second transaction gets a version mismatch (`ObjectOptimisticLockingFailureException`) and the API returns a retry message.

I went with optimistic over pessimistic locking because most events won't have heavy contention on every purchase. Pessimistic (`SELECT FOR UPDATE`) would make more sense if we had constant last-ticket races, but optimistic gives better throughput for the general case.

**Security:** JWT for stateless auth. ADMIN manages events, USER buys tickets. Passwords stored with BCrypt.

**Caching:** Simple in-memory cache on event listing, evicted on create/update/delete.

**12 Factor:**
- Config from environment variables (DB, JWT secret, port)
- Graceful shutdown enabled
- Logs to stdout

## Project Structure

```
src/main/java/com/tcs/eventhub/
├── config/          # DataLoader
├── controller/      # REST endpoints
├── domain/
│   ├── entity/      # JPA entities
│   └── repository/  # Data access
├── dto/             # Request/Response records
├── exception/       # Error handling
├── security/        # JWT + Security config
└── service/         # Business logic
```
