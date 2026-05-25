# IT342--Garbo-PawnScan

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

3. Edit `.env` and set real values for `DB_PASSWORD`, `JWT_SECRET`, `GOOGLE_CLIENT_ID`, and SMTP email delivery.
4. Start PostgreSQL and make sure the database in `DB_URL` exists.
5. Run the backend from `backend/pawnscan`.

```bash
./mvnw spring-boot:run
```

Notes:

- `application.properties` reads `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `GOOGLE_CLIENT_ID`, and SMTP settings from environment values.
- For SendGrid SMTP, set `SMTP_HOST=smtp.sendgrid.net`, `SMTP_USERNAME=apikey`, `SMTP_PASSWORD` or `SENDGRID_API_KEY` to your API key, and `APP_MAIL_FROM` to a verified sender.
- For Gmail SMTP, set `SMTP_HOST=smtp.gmail.com`, `SMTP_USERNAME` and `APP_MAIL_FROM` to the Gmail address, and `SMTP_PASSWORD` to a Google app password.
- For Google OAuth on localhost, create a Google OAuth Web Client in Google Cloud Console and add `http://localhost:3000` to Authorized JavaScript origins.
- If you run Next.js directly from `web`, set `NEXT_PUBLIC_GOOGLE_CLIENT_ID` in `web/.env.local` or set `GOOGLE_CLIENT_ID` in the same shell before starting `npm run dev`.

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

2. Edit `.env` and set secure `JWT_SECRET` and `GOOGLE_CLIENT_ID` values.
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
