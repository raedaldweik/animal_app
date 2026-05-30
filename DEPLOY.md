# Deployment guide

The app is a single self-contained container. It binds to the `PORT` environment
variable that PaaS platforms inject, exposes a health check at
`/actuator/health`, and works with **either** the embedded H2 database (zero
config) **or** a managed PostgreSQL (set a few env vars).

Below is a step-by-step for **Railway**, plus quick notes for Render and Fly.io.

---

## Railway (recommended, ~3 minutes)

Railway auto-detects the `Dockerfile` (and `railway.json` pins it explicitly).

### Option A — Simplest: app only, embedded H2

Good for a quick live demo. Data is **ephemeral** (resets on redeploy).

1. Go to <https://railway.app> → **New Project** → **Deploy from GitHub repo**.
2. Pick `raedaldweik/animal_app`. Railway builds the `Dockerfile` automatically.
3. Once deployed, open **Settings → Networking → Generate Domain**.
4. Visit the generated URL — the futuristic UI loads at `/`.

That's it. No environment variables required.

### Option B — Persistent: app + PostgreSQL

1. Same as above to create the service from the repo.
2. In the project, click **New → Database → Add PostgreSQL**.
3. Open the **app service → Variables** and add:

   | Variable | Value |
   |---|---|
   | `SPRING_PROFILES_ACTIVE` | `docker` |
   | `DB_HOST` | `${{Postgres.PGHOST}}` |
   | `DB_PORT` | `${{Postgres.PGPORT}}` |
   | `DB_NAME` | `${{Postgres.PGDATABASE}}` |
   | `DB_USER` | `${{Postgres.PGUSER}}` |
   | `DB_PASSWORD` | `${{Postgres.PGPASSWORD}}` |

   (`${{Postgres.*}}` are Railway reference variables — type them as-is and Railway
   wires them to the database service.)
4. Redeploy. Generate a domain (Settings → Networking) and open it.

> The app needs outbound internet access to reach the image providers
> (cataas.com, dog.ceo, loremflickr.com) — Railway allows this by default.

---

## Render

1. <https://render.com> → **New → Web Service** → connect the repo.
2. Runtime: **Docker** (Render reads the `Dockerfile`).
3. For persistence: add a **Render PostgreSQL** instance and set the same
   `SPRING_PROFILES_ACTIVE=docker` + `DB_*` variables (Render exposes
   `host`, `port`, `database`, `user`, `password` on the DB dashboard).
4. Health check path: `/actuator/health`.

## Fly.io

```bash
fly launch            # detects the Dockerfile; sets internal_port = 8080
fly postgres create   # optional managed Postgres
fly postgres attach   # then map its vars to DB_HOST/DB_PORT/... with `fly secrets set`
fly deploy
```

---

## Verifying a deployment

```bash
# health
curl https://<your-domain>/actuator/health      # -> {"status":"UP"}

# fetch & store 2 cats
curl -X POST "https://<your-domain>/api/v1/pictures?type=cat&count=2"

# get the last stored cat image
curl "https://<your-domain>/api/v1/pictures/cat/last" --output cat.jpg
```

Or just open `https://<your-domain>/` and use the UI.
