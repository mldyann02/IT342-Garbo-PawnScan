# IT342-Garbo-PawnScan

## Local Setup (Backend)

1. Clone the repository.
2. Create your local environment file from the template.

Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

macOS/Linux/Git Bash:

```bash
cp .env.example .env
```

3. Edit `.env` and set real values for `DB_PASSWORD` and `JWT_SECRET`.
4. Start PostgreSQL and make sure the database in `DB_URL` exists.
5. Run the backend from `backend/pawnscan`.

```bash
./mvnw spring-boot:run
```

Notes:

- `application.properties` reads `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `JWT_SECRET` from environment values.

## Docker Setup (Web + Backend + PostgreSQL)

1. Copy the env template to `.env`.

Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

macOS/Linux/Git Bash:

```bash
cp .env.example .env
```

2. Edit `.env` and set a secure `JWT_SECRET` value.
3. Start all services from the repository root.

```bash
docker compose up --build
```

4. Open the apps:

- Web: `http://localhost:3000`
- Backend API: `http://localhost:8080`

Useful commands:

```bash
docker compose down
docker compose down -v
```

- `docker compose down` stops containers.
- `docker compose down -v` also removes local Docker volumes (database data and uploaded files).
