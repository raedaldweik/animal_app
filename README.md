# 🐾 Animal Picture App

[![CI](https://github.com/raedaldweik/animal_app/actions/workflows/ci.yml/badge.svg)](https://github.com/raedaldweik/animal_app/actions/workflows/ci.yml)

A small Spring Boot microservice that fetches random pictures of **cats, dogs, and bears**,
stores them in a database, and serves the most recently stored picture back on demand.
Ships with a futuristic web UI.

Built for the Camunda TAM technical challenge.

---

## Features

| Requirement | Status |
|---|---|
| REST endpoint to fetch & save *N* pictures of an animal type | ✅ |
| REST endpoint to fetch the last stored picture of an animal | ✅ |
| Pictures stored in a database | ✅ (H2 by default, PostgreSQL in containers) |
| Containerized | ✅ (`Dockerfile` + `docker compose`) |
| README | ✅ |
| **Bonus:** automated tests | ✅ (unit + integration, 13 tests) |
| **Bonus:** simple UI | ✅ (static page at `/`) |
| Portable build via Maven, shipped DB, runs containerized **and** non-containerized | ✅ |

---

## Quick start

### Option A — Run locally (non-containerized, zero setup)

Requires **JDK 21**. Maven is not needed — the project ships the Maven wrapper.

```bash
./mvnw spring-boot:run
```

The app starts on <http://localhost:8080>. It uses a file-based **H2** database
(written to `./data/`), so there is nothing else to install.

Open the UI at <http://localhost:8080/>.

### Option B — Run containerized (app + PostgreSQL)

Requires **Docker** with the Compose plugin.

```bash
docker compose up --build
```

This builds the app image and starts it alongside a **PostgreSQL** container
(the app runs under the `docker` Spring profile). The UI is again at
<http://localhost:8080/>.

To stop and remove everything (including the DB volume):

```bash
docker compose down -v
```

### Option C — Deploy to the cloud (live URL)

The image binds to the platform's `PORT` and exposes `/actuator/health`, so it
deploys to Railway/Render/Fly.io as-is. See **[DEPLOY.md](DEPLOY.md)** — the
simplest path is "Deploy from GitHub repo" on Railway (no config needed).

---

## API reference

Base path: `/api/v1/pictures`

### 1. Fetch & store pictures

```
POST /api/v1/pictures?type={cat|dog|bear}&count={1..50}
```

`count` defaults to `1`. Fetches `count` random pictures of `type` from the
upstream provider, stores them, and returns their metadata.

```bash
curl -X POST "http://localhost:8080/api/v1/pictures?type=cat&count=2"
```

```json
{
  "type": "cat",
  "count": 2,
  "pictures": [
    {
      "id": 1,
      "type": "CAT",
      "contentType": "image/jpeg",
      "sourceUrl": "https://cataas.com/cat",
      "sizeBytes": 51234,
      "createdAt": "2026-05-29T15:00:00Z",
      "url": "/api/v1/pictures/1"
    },
    { "id": 2, "type": "CAT", "url": "/api/v1/pictures/2", "...": "..." }
  ]
}
```

### 2. Get the last stored picture (image bytes)

```
GET /api/v1/pictures/{type}/last
```

Returns the raw image bytes of the most recently stored picture for that type
(`Content-Type` reflects the stored image). `404` if none stored yet.

```bash
curl "http://localhost:8080/api/v1/pictures/cat/last" --output last-cat.jpg
```

### 3. Get the last stored picture metadata (JSON)

```
GET /api/v1/pictures/{type}/last/info
```

### 4. Get a specific stored picture by id (image bytes)

```
GET /api/v1/pictures/{id}
```

### Health

```
GET /actuator/health
```

### Error handling

Errors are returned as [RFC 7807](https://datatracker.ietf.org/doc/html/rfc7807)
`application/problem+json`:

| Situation | Status |
|---|---|
| Unknown animal type | `400 Bad Request` |
| `count` outside `1..50` | `400 Bad Request` |
| No picture stored yet | `404 Not Found` |
| Upstream image provider failed | `502 Bad Gateway` |

---

## Design & architecture

```
web (REST controllers, DTOs, error handling)
        │
service (orchestration, transactions)
      ┌─┴─────────────────────────┐
repository (Spring Data JPA)   provider (pluggable image sources)
        │                              │
   H2 / PostgreSQL            cataas.com / dog.ceo / loremflickr.com
```

**Key decisions**

- **Java 21 + Spring Boot 3 + Maven** — aligns with Camunda's Java focus and
  produces a single portable, self-contained jar.
- **Pluggable provider abstraction** (`AnimalImageProvider`). Each animal has its
  own provider, so different upstream styles are cleanly isolated — the dog
  provider does a two-step *JSON → image* fetch, while cats/bears return image
  bytes directly. Adding a new animal is just a new provider bean.
- **Configurable endpoints.** The placeholder services suggested in the challenge
  (`placekitten.com`, `place.dog`, `placebear.com`) are largely defunct, and the
  challenge explicitly allows alternatives. The defaults are healthy services
  ([cataas.com](https://cataas.com), [dog.ceo](https://dog.ceo),
  [loremflickr.com](https://loremflickr.com)), and every URL is overridable via
  config/env (see below) without code changes.
- **Images stored as bytes in the DB** (`byte[]` → `VARBINARY` on H2, `bytea` on
  PostgreSQL). Plain `byte[]` is used instead of `@Lob` to avoid PostgreSQL
  large-object pitfalls while staying portable.
- **Two run modes from one codebase** via Spring profiles: default = embedded H2
  (zero setup); `docker` = shipped PostgreSQL.

---

## Configuration

| Property | Env var | Default |
|---|---|---|
| `animalapp.providers.cat-url` | `ANIMALAPP_PROVIDERS_CAT_URL` | `https://cataas.com/cat` |
| `animalapp.providers.dog-api-url` | `ANIMALAPP_PROVIDERS_DOG_API_URL` | `https://dog.ceo/api/breeds/image/random` |
| `animalapp.providers.bear-url` | `ANIMALAPP_PROVIDERS_BEAR_URL` | `https://loremflickr.com/640/480/bear` |

PostgreSQL (used by the `docker` profile) is configured via `DB_HOST`, `DB_PORT`,
`DB_NAME`, `DB_USER`, `DB_PASSWORD`.

> **Note:** the app must be able to reach the chosen image APIs at runtime. In a
> locked-down network you may need to allowlist the provider hosts or point the
> URLs above at internal mirrors.

---

## Tests

```bash
./mvnw verify
```

- **Unit tests** — animal-type parsing, the service orchestration logic
  (Mockito), and each provider's HTTP behaviour against an OkHttp `MockWebServer`
  (including the dog two-step fetch and upstream-failure handling).
- **Integration test** (`*IT`, run by the Failsafe plugin) — boots the full
  Spring context with an in-memory H2 database and a `MockWebServer` standing in
  for the upstream provider, then exercises the fetch/store and last-picture
  flows plus validation errors end to end.

`mvn test` runs the unit tests; `mvn verify` additionally runs the integration test.

---

## Project layout

```
src/main/java/com/example/animalapp
├── AnimalAppApplication.java
├── config/        # shared RestClient (timeouts)
├── domain/        # AnimalType, AnimalPicture entity
├── provider/      # AnimalImageProvider + cat/dog/bear impls, registry, config
├── repository/    # Spring Data JPA repository
├── service/       # AnimalPictureService (fetch/store orchestration)
└── web/           # REST controller, DTOs, error handling
src/main/resources
├── application.yml         # default profile (H2)
├── application-docker.yml  # docker profile (PostgreSQL)
└── static/index.html       # simple UI
```

---

## Notes & possible improvements

Within the ~2-hour scope I prioritized a clean, fully working core (both
endpoints, both run modes, tests, and a UI). With more time I would add:

- **Pagination / history endpoints** (list all pictures for a type), and
  optional dedup of identical images.
- **Resilience**: retries with backoff and a circuit breaker (Resilience4j)
  around upstream calls, since placeholder image services can be flaky.
- **Object storage** (e.g. S3/MinIO) for the image bytes, keeping only metadata
  in the relational DB, which scales better than storing blobs in the DB.
- **OpenAPI/Swagger UI** (springdoc) for live API docs.
- **DB migrations** with Flyway instead of Hibernate `ddl-auto` for production.
- **Observability**: Micrometer metrics + structured logging.
