![CI](https://github.com/PedroAMDC/music-catalog/actions/workflows/ci.yml/badge.svg)

[Versao em portugues](README.pt-BR.md)

# Music Catalog - Artist and Album Management System

## Tech Stack

### Backend
- **Language:** Java 21
- **Framework:** Quarkus
- **Build Tool:** Maven
- **Database:** PostgreSQL 16
- **Storage:** MinIO (S3 Compatible)
- **Authentication:** JWT (SmallRye JWT)
- **Documentation:** OpenAPI/Swagger
- **Migrations:** Flyway

### Frontend
- **Framework:** React 19 + Next.js 16
- **Language:** TypeScript
- **Styling:** Tailwind CSS + shadcn/ui
- **State Management:** React Context + Hooks (Facade Pattern)

### Infrastructure
- **Containerization:** Docker + Docker Compose
- **CI/CD:** GitHub Actions

---

## Getting Started

### Prerequisites

| Requirement | Minimum Version | Check Installation |
|-------------|-----------------|-------------------|
| Docker | 20.10+ | `docker --version` |
| Docker Compose | v2.0+ | `docker compose version` |
| Git | 2.30+ | `git --version` |

**Recommended resources:** 4 GB of available RAM for the containers.

**Required ports (must be free):**

| Port | Service |
|------|---------|
| 3000 | Frontend (Next.js) |
| 5432 | PostgreSQL |
| 8080 | Backend (Quarkus) |
| 9000 | MinIO (API) |
| 9001 | MinIO (Console) |

### Clone and Setup

**1. Clone the repository:**

```bash
git clone https://github.com/PedroAMDC/music-catalog.git
cd music-catalog
```

**2. Configure environment variables:**

```bash
cp .env.example .env
```

The `.env.example` file contains default values ready for local use. Edit `.env` only if you need to customize any configuration (see the environment variables table below).

**3. Start all services:**

```bash
docker compose up --build
```

Wait until all containers are healthy. The backend may take a few seconds to start after PostgreSQL and MinIO are ready. Initialization is complete when you see in the logs:

```
artistas-backend  | Quarkus started in ...
artistas-frontend | Ready in ...
```

### RSA Keys (local development only)

> **Note:** When using `docker compose up --build`, RSA keys are configured automatically during the build. The step below is only needed if you want to run the backend locally (outside Docker).

```bash
cd backend/src/main/resources
cp privateKey.example.pem privateKey.pem
cp publicKey.example.pem publicKey.pem
cd ../../../..
```

To generate new keys (production):

```bash
cd backend/src/main/resources
openssl genrsa -out privateKey.pem 2048
openssl rsa -in privateKey.pem -pubout -out publicKey.pem
cd ../../../..
```

### Environment Variables

All variables are documented in the `.env.example` file. The table below describes each one:

**PostgreSQL:**

| Variable | Description | Default |
|----------|-------------|---------|
| `POSTGRES_HOST` | Database host | `postgres` |
| `POSTGRES_PORT` | Database port | `5432` |
| `POSTGRES_DB` | Database name | `artistas_albuns` |
| `POSTGRES_USER` | Database user | `postgres` |
| `POSTGRES_PASSWORD` | Database password | `postgres123` |

**MinIO (Object Storage):**

| Variable | Description | Default |
|----------|-------------|---------|
| `MINIO_ENDPOINT` | Internal MinIO service URL (Docker) | `http://minio:9000` |
| `MINIO_PUBLIC_ENDPOINT` | Public MinIO URL (browser accessible) | `http://localhost:9000` |
| `MINIO_ROOT_USER` | MinIO root user | `minioadmin` |
| `MINIO_ROOT_PASSWORD` | MinIO root password | `minioadmin123` |
| `MINIO_BUCKET` | Bucket name for covers | `albuns-capas` |
| `MINIO_PRESIGNED_URL_EXPIRY` | Presigned URL expiration (seconds) | `1800` |

**Backend (Quarkus):**

| Variable | Description | Default |
|----------|-------------|---------|
| `QUARKUS_HTTP_PORT` | Server port | `8080` |
| `QUARKUS_PROFILE` | Execution profile | `dev` |
| `JWT_SECRET` | JWT signing secret | (long value in .env.example) |
| `JWT_ISSUER` | JWT token issuer | `artistas-albuns-api` |
| `JWT_EXPIRATION_SECONDS` | Access token expiration | `300` (5 min) |
| `JWT_REFRESH_EXPIRATION_SECONDS` | Refresh token expiration | `86400` (24h) |
| `CORS_ALLOWED_ORIGINS` | Allowed CORS origins | `http://localhost:3000` |
| `RATE_LIMIT_REQUESTS_PER_MINUTE` | Rate limit per user per minute | `10` |

**Frontend (Next.js):**

| Variable | Description | Default |
|----------|-------------|---------|
| `NEXT_PUBLIC_API_URL` | Backend API URL | `http://localhost:8080` |
| `NEXT_PUBLIC_WS_URL` | WebSocket URL | `ws://localhost:8080` |

**External API:**

| Variable | Description | Default |
|----------|-------------|---------|
| `EXTERNAL_API_URL` | Public regional API URL | `https://api-publica-mt.seplag.mt.gov.br` |

### Docker Compose Commands

```bash
# Start all services (first use or after changes)
docker compose up --build

# Start in detached mode
docker compose up --build -d

# Stop all services
docker compose down

# Stop and remove volumes (full database and MinIO reset)
docker compose down -v

# View logs for all services
docker compose logs -f

# View logs for a specific service
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f postgres
docker compose logs -f minio

# Restart a specific service
docker compose restart backend

# Rebuild and restart only one service
docker compose up --build -d backend

# Check container status
docker compose ps
```

### Service URLs

| Service | URL |
|---------|-----|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/q/swagger-ui |
| Health Check | http://localhost:8080/q/health |
| MinIO Console | http://localhost:9001 |

### Default Credentials

**Application (register a new user via frontend or API):**

```bash
curl -X POST http://localhost:8080/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@test.com","password":"Pass@123","confirmPassword":"Pass@123","nome":"Admin"}'
```

**MinIO Console (http://localhost:9001):**
- User: `minioadmin`
- Password: `minioadmin123`

**PostgreSQL (localhost:5432):**
- Database: `artistas_albuns`
- User: `postgres`
- Password: `postgres123`

### Troubleshooting

**Port already in use:**

```
Error: bind: address already in use
```

Check which processes are using the required ports:

```bash
# Linux/macOS
lsof -i :3000 -i :5432 -i :8080 -i :9000 -i :9001

# Windows (PowerShell)
netstat -ano | findstr "3000 5432 8080 9000 9001"
```

Kill the conflicting process or change the ports in `docker-compose.yml`.

**Backend cannot connect to PostgreSQL:**

```
Connection refused / FATAL: database "artistas_albuns" does not exist
```

PostgreSQL may not have finished initializing. Wait and try again. If the issue persists, restart with clean volumes:

```bash
docker compose down -v
docker compose up --build
```

**RSA key / JWT error:**

```
java.security.spec.InvalidKeySpecException / Could not read private key
```

If using Docker, rebuild the backend image: `docker compose up --build backend`. If running locally, copy the example keys as described in the "RSA Keys" section above.

**MinIO bucket not found:**

```
The specified bucket does not exist
```

The `minio-setup` container creates the bucket automatically. Check if it ran successfully:

```bash
docker compose logs minio-setup
```

If needed, create it manually via MinIO Console (http://localhost:9001) with the name `albuns-capas`.

**Frontend cannot connect to API:**

```
ECONNREFUSED / Network Error
```

Check if the backend is running and healthy:

```bash
curl http://localhost:8080/q/health
```

On Windows with Docker Desktop, make sure `localhost` is accessible from within the container.

**Docker out of memory:**

```
Exited (137) / OOMKilled
```

Increase Docker Desktop memory (Settings > Resources) to at least 4 GB.

**Full rebuild (when nothing works):**

```bash
docker compose down -v
docker system prune -f
docker compose up --build
```

---

## Architecture

### Backend (Quarkus)

```
backend/
├── src/main/java/com/artistas/
│   ├── api/v1/          # REST Endpoints
│   ├── config/          # Configuration (CORS, Security)
│   ├── filters/         # Rate Limit Filter
│   ├── models/          # JPA Entities
│   ├── schemas/         # DTOs
│   ├── services/        # Business Logic
│   └── websocket/       # WebSocket Handlers
├── src/main/resources/
│   ├── application.properties
│   └── db/migration/    # Flyway Migrations
└── src/test/            # Tests
```

### Frontend (Next.js)

```
frontend/
├── app/                 # Next.js App Router
│   ├── (auth)/          # Authentication routes
│   ├── artistas/        # Artist pages
│   └── albuns/          # Album pages
├── components/          # React Components
│   ├── ui/              # shadcn/ui components
│   └── common/          # Reusable components
├── lib/                 # Services (Facade Pattern)
├── hooks/               # Custom Hooks
├── contexts/            # React Contexts
└── types/               # TypeScript Types
```

### System Diagram

```
+-------------------+
|     Browser       |
+--------+----------+
         |
         | HTTP :3000
         v
+-------------------+     WebSocket
|    Frontend       |------------------+
|   Next.js 16      |                  |
+--------+----------+                  |
         |                             |
         | HTTP/REST :8080             |
         v                             v
+-------------------+     +-------------------+
|    Backend        |     |    WebSocket      |
|   Quarkus 3.17    |     | /ws/notifications |
+--------+----------+     +-------------------+
         |
    +----+----+
    |         |
    v         v
+-------+  +-------+
|  DB   |  | MinIO |
| PgSQL |  |  S3   |
| :5432 |  | :9000 |
+-------+  +-------+
```

### Data Model (ER)

```
+---------------+       +------------------+       +---------------+
|   Usuario     |       |  artista_album   |       |    Artista    |
+---------------+       +------------------+       +---------------+
| id (PK)       |       | artista_id (FK)  |------>| id (PK)       |
| email (UQ)    |       | album_id (FK)    |       | nome          |
| password_hash |       +------------------+       | tipo          |
| nome          |               |                  | created_at    |
| ativo         |               |                  +---------------+
| created_at    |               |                         ^
+---------------+               v                         |
                        +---------------+                 |
                        |    Album      |-----------------+
                        +---------------+        N:M
                        | id (PK)       |
                        | titulo        |
                        | ano_lancamento|
                        | created_at    |
                        +-------+-------+
                                |
                                | 1:N
                                v
                        +---------------+
                        |  CapaAlbum    |
                        +---------------+
                        | id (PK)       |
                        | album_id (FK) |
                        | minio_key     |
                        | original_name |
                        | content_type  |
                        | tamanho_bytes |
                        +---------------+
```

### Authentication Flow (JWT)

```
+--------+          +----------+          +---------+
| Client |          | Backend  |          |   DB    |
+---+----+          +----+-----+          +----+----+
    |                    |                     |
    | POST /v1/auth/login                      |
    |------------------->|                     |
    |                    | Find user           |
    |                    |-------------------->|
    |                    |<--------------------|
    |                    |                     |
    |                    | Validate password (BCrypt)
    |                    | Generate JWT (5min)  |
    |                    | Generate Refresh (24h)|
    |<-------------------|                     |
    | { accessToken, refreshToken }            |
    |                    |                     |
    | GET /v1/artistas   |                     |
    | Authorization: Bearer <token>            |
    |------------------->|                     |
    |                    | Validate JWT        |
    |                    | Rate Limit Check    |
    |                    |-------------------->|
    |<-------------------|                     |
    | { artistas[] }     |                     |
    |                    |                     |
    | PUT /v1/auth/refresh                     |
    | { refreshToken }   |                     |
    |------------------->|                     |
    |                    | Validate Refresh    |
    |                    | Generate new pair   |
    |<-------------------|                     |
    | { accessToken, refreshToken }            |
+---+----+          +----+-----+          +----+----+
```

---

## Features

### General Requirements
- [x] CORS configured
- [x] JWT authentication (5 min expiration + refresh)
- [x] POST, PUT, GET endpoints
- [x] Album query pagination
- [x] Parameterized queries (singers/bands)
- [x] Alphabetical sorting (asc/desc)
- [x] Cover image upload
- [x] MinIO storage
- [x] Presigned URLs (30 min expiration)
- [x] Endpoint versioning (/v1/)
- [x] Flyway Migrations
- [x] OpenAPI/Swagger documentation

### Senior Requirements
- [x] Health Checks (Liveness/Readiness)
- [x] Unit tests
- [x] WebSocket notifications
- [x] Rate limit (10 req/min per user)
- [x] Regional data synchronization

### Frontend
- [x] Responsive layout
- [x] Tailwind CSS
- [x] Lazy Loading Routes
- [x] Pagination
- [x] TypeScript
- [x] Facade Pattern
- [x] State management
- [x] Unit tests

---

## Seed Data

The database is automatically populated with the following data:

| Artist | Albums |
|--------|--------|
| Serj Tankian | Harakiri, Black Blooms, The Rough Dog |
| Mike Shinoda | The Rising Tied, Post Traumatic, Post Traumatic EP, Where'd You Go |
| Michel Telo | Bem Sertanejo, Bem Sertanejo - O Show (Ao Vivo), Bem Sertanejo - (1a Temporada) - EP |
| Guns N' Roses | Use Your Illusion I, Use Your Illusion II, Greatest Hits |

---

## Technical Decisions

### Why Quarkus?
- Superior startup and runtime performance
- Container-first design ideal for Docker
- Excellent extension support (JWT, Flyway, OpenAPI)
- Developer experience with hot-reload

### Why Next.js?
- Optimized builds with Turbopack
- App Router with Server Components
- Native lazy loading
- Excellent TypeScript integration

### Why React Context + Hooks instead of BehaviorSubject?
- BehaviorSubject is an RxJS concept, native to the Angular ecosystem
- In React, the idiomatic equivalent is Context API + custom Hooks, which provides reactivity and state management natively without external dependencies
- Services in `lib/` implement the Facade Pattern, abstracting API calls and keeping components decoupled from business logic
- `RateLimitEventEmitter` implements the Observer pattern (subscribe/emit/unsubscribe), functionally equivalent to BehaviorSubject
- TypeScript provides complete typing for Context and Hooks without additional configuration

### Why MinIO?
- 100% S3-compatible API
- Easy to run locally via Docker
- Native presigned URL support

---

## Running Tests

### Backend
```bash
cd backend
./mvnw test
```

### Frontend
```bash
cd frontend
npm test
```

---

## Commits

This project follows **semantic commits** to maintain a clear and consistent history.

### Message Format

```
<type>: <description>
```

**Rules:**
- Messages always in **English**
- Use **imperative** verbs (add, fix, update, remove)
- Maximum **72 characters** on the first line
- Clear and objective description of what changed

### Commit Types

| Type | Description | Example |
|------|-------------|---------|
| `feat:` | New feature | `feat: add artist image upload endpoint` |
| `fix:` | Bug fix | `fix: resolve album pagination returning wrong count` |
| `chore:` | Administrative tasks | `chore: update quarkus dependencies to 3.17` |
| `docs:` | Documentation | `docs: add commit guidelines to README` |
| `test:` | Test additions or changes | `test: add unit tests for ArtistaService` |
| `refactor:` | Refactoring without behavior change | `refactor: extract validation logic to separate class` |

### Branch Convention

| Pattern | Use | Example |
|---------|-----|---------|
| `feature/T0XX-description` | New features | `feature/T015-add-album-search` |
| `fix/T0XX-description` | Bug fixes | `fix/T023-pagination-offset` |

### Git Flow

```
feature/* or fix/*  -->  develop  -->  main
```

- **develop:** Main development branch
- **main:** Production branch (stable releases)
- All features and fixes must be merged via **Pull Request**

---

## Methodology

This project was developed following agile practices:

- **Kanban Board:** Backlog organized in [GitHub Projects](https://github.com/PedroAMDC/music-catalog/projects) with To Do, In Progress and Done columns
- **Incremental tasks:** Each feature was broken down into small, well-defined tasks with clear acceptance criteria
- **Semantic commits:** Organized and descriptive commit history
- **Feature branches:** Each task developed in an isolated branch with merge via Pull Request
- **Mandatory tests:** Unit test coverage as a requirement for task completion

---

## License

MIT

---

```
██████╗░███████╗██████╗░██████╗░░█████╗░░█████╗░███╗░░░███╗██████╗░░█████╗░
██╔══██╗██╔════╝██╔══██╗██╔══██╗██╔══██╗██╔══██╗████╗░████║██╔══██╗██╔══██╗
██████╔╝█████╗░░██║░░██║██████╔╝██║░░██║███████║██╔████╔██║██║░░██║██║░░╚═╝
██╔═══╝░██╔══╝░░██║░░██║██╔══██╗██║░░██║██╔══██║██║╚██╔╝██║██║░░██║██║░░██╗
██║░░░░░███████╗██████╔╝██║░░██║╚█████╔╝██║░░██║██║░╚═╝░██║██████╔╝╚█████╔╝
╚═╝░░░░░╚══════╝╚═════╝░╚═╝░░╚═╝░╚════╝░╚═╝░░╚═╝╚═╝░░░░░╚═╝╚═════╝░░╚════╝░
```

[GitHub](https://github.com/PedroAMDC)
