# Lost Item Portal

A full-stack lost-item reporting portal with:
- **Frontend:** React + TypeScript + Vite + MUI
- **Backend:** Spring Boot (Java 21) + JPA
- **Database:** PostgreSQL
- **Orchestration:** Docker Compose

## Features

- View all lost item reports
- Register and login users
- Create a new report (authenticated)
- Edit an existing report (owner-only)
- Delete a report (owner-only)
- Track report status (`Still Looking`, `Found`, `Claimed`)
- View item details with status activity log
- Update status as item owner or admin

## Project structure

```text
lost_item_portal/
├── backend/        # Spring Boot REST API
├── frontend/       # React app
├── docker-compose.yml
├── .env.example
└── README.md
```

## Environment variables

Copy `.env.example` to `.env` and adjust values as needed:

```env
DB_USER=postgres
DB_PASS=your_password_here
DB_NAME=lost_items
DB_PORT=5432
VITE_API_URL=http://localhost:8080/api
APP_CORS_ALLOWED_ORIGINS=http://localhost:3000,http://127.0.0.1:3000
AUTH_JWT_SECRET=replace-this-with-a-very-long-secret-key-for-jwt-signing-please-change
AUTH_JWT_ACCESS_TOKEN_EXPIRATION_SECONDS=900
AUTH_REFRESH_TOKEN_EXPIRATION_SECONDS=1209600
AUTH_REFRESH_COOKIE_SECURE=false
AUTH_REFRESH_COOKIE_SAME_SITE=Lax
```

> If port `5432` is already used on your machine, set `DB_PORT` to another port (for example `5433`).

## Run with Docker Compose (recommended)

```bash
docker compose up --build -d
```

Access:
- Frontend: `http://localhost:3000`
- Backend API: `http://localhost:8080/api`
- PostgreSQL: `localhost:${DB_PORT}`

Stop:

```bash
docker compose down
```

## Run locally (without Docker)

### Backend

```bash
cd backend
sh ./mvnw spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

## Quality checks

### Frontend

```bash
cd frontend
npm run lint
npm run build
```

### Backend tests (Java 21 required)

```bash
cd backend
sh ./mvnw test
```

## Authentication flow

1. Register: `POST /api/auth/register` with `email` and `password` (min 8 chars).
2. Login: `POST /api/auth/login` with credentials.
3. Backend returns:
   - short-lived JWT access token in response body
   - refresh token in `HttpOnly` cookie (`refreshToken`)
4. Frontend stores access token in memory and sends it as `Authorization: Bearer <token>` for protected calls.
5. On reload/session restore, frontend calls `POST /api/auth/refresh` (cookie-based) to get a new access token.
6. Logout: `POST /api/auth/logout` revokes refresh token and clears cookie.

## API authorization rules

| Endpoint | Access |
| --- | --- |
| `GET /api/lost-items` | Public |
| `GET /api/lost-items/{id}` | Public |
| `GET /api/lost-items/{id}/details` | Public |
| `POST /api/lost-items` | Authenticated |
| `PUT /api/lost-items/{id}` | Authenticated + owner only |
| `DELETE /api/lost-items/{id}` | Authenticated + owner only |
| `PATCH /api/lost-items/{id}/status` | Authenticated + owner or admin |
| `/api/auth/*` | Public (except `/api/auth/me` requires auth) |

### Lost item status

- Status values: `STILL_LOOKING`, `FOUND`, `CLAIMED`
- New reports default to `STILL_LOOKING` if status is omitted
- Status changes are recorded and returned in `GET /api/lost-items/{id}/details`
