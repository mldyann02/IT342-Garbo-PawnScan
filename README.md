# IT342--Garbo-PawnScan

PawnScan is a pawnshop-focused item verification system with a Spring Boot
backend, a Next.js web app, and an Android mobile app. It supports account
authentication, Google sign-in, item/report workflows, file uploads, email
delivery, and public stolen-item lookup integration.

## Project Structure

```text
backend/pawnscan/   Spring Boot API
web/                Next.js web client
mobile/             Android app
docs/               Project documentation
uploads/            Local upload storage
```

## Tech Stack

- Backend: Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA,
  PostgreSQL, JWT, JavaMail, Firebase Admin, Google OAuth.
- Web: Next.js 14, React 18, TypeScript, Tailwind CSS, Vitest.
- Mobile: Android Kotlin, XML/ViewBinding, Retrofit, Firebase Messaging,
  Google Sign-In.
- Infrastructure: Docker Compose for PostgreSQL, backend, and web services.

## Requirements

- Java 17
- Node.js 20 or a compatible current LTS version
- npm
- Docker Desktop, if using Docker setup
- PostgreSQL 16 or compatible, if running the backend without Docker
- Android Studio, Android SDK, and an emulator/device for mobile development

## Environment Setup

Copy the sample environment file before running the backend or Docker setup.

Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

macOS/Linux/Git Bash:

```bash
cp .env.example .env
```

Important values to configure:

- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `POSTGRES_DB` for PostgreSQL.
- `JWT_SECRET` with a secure random value of at least 32 characters.
- `GOOGLE_CLIENT_ID` or `GOOGLE_WEB_CLIENT_ID` for Google OAuth.
- SMTP settings such as `SMTP_HOST`, `SMTP_USERNAME`, `SMTP_PASSWORD`, and
  `APP_MAIL_FROM` for email delivery.
- `NEXT_PUBLIC_GOOGLE_CLIENT_ID` when running the web app directly from
  `web/`.
- `FIREBASE_CREDENTIALS` if Firebase Admin credentials are needed by the
  backend.

## Local Setup (Backend)

1. Clone the repository.
2. Create and configure `.env`.
3. Start PostgreSQL and make sure the database in `DB_URL` exists.
4. Run the backend from `backend/pawnscan`.

```bash
./mvnw spring-boot:run
```

Notes:

- `application.properties` reads `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `GOOGLE_WEB_CLIENT_ID` or legacy `GOOGLE_CLIENT_ID`, and SMTP settings from environment values.
- For SendGrid SMTP, set `SMTP_HOST=smtp.sendgrid.net`, `SMTP_USERNAME=apikey`, `SMTP_PASSWORD` or `SENDGRID_API_KEY` to your API key, and `APP_MAIL_FROM` to a verified sender.
- For Gmail SMTP, set `SMTP_HOST=smtp.gmail.com`, `SMTP_USERNAME` and `APP_MAIL_FROM` to the Gmail address, and `SMTP_PASSWORD` to a Google app password.
- For Google OAuth, create a Web application OAuth client and set its client ID as `GOOGLE_WEB_CLIENT_ID`. For Android Google Sign-In, also create an Android OAuth client in the same Google Cloud project using package `com.cit.pawnscan` and your debug SHA-1 from `mobile/gradlew.bat :app:signingReport`.
- If you run Next.js directly from `web`, set `NEXT_PUBLIC_GOOGLE_CLIENT_ID` in `web/.env.local` or set `GOOGLE_CLIENT_ID` in the same shell before starting `npm run dev`.

## Local Setup (Web)

Run the web client from `web`.

```bash
npm install
npm run dev
```

Open `http://localhost:3000`. The web app expects the backend to be available
at `http://localhost:8080` by default. Override this with
`NEXT_PUBLIC_BACKEND_URL` when needed.

Useful web commands:

```bash
npm run build
npm run start
npm run lint
npm run test
```

## Local Setup (Mobile)

Open `mobile/` in Android Studio, or use the Gradle wrapper from the `mobile`
directory.

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat test
```

The Android app reads `PAWNSCAN_API_BASE_URL` from `mobile/local.properties`.
The default is `http://10.0.2.2:8080`, which points an Android emulator to the
host machine. For a physical device, set it to your computer's LAN address, for
example:

```properties
PAWNSCAN_API_BASE_URL=http://192.168.1.10:8080
```

For Firebase and Google Sign-In, keep `google-services.json` in
`mobile/app/` and use package name `com.cit.pawnscan`.

## Docker Setup (Web + Backend + PostgreSQL)

1. Create and configure `.env`.
2. Start all services from the repository root.

```bash
docker compose up --build
```

3. Open the apps:

- Web: `http://localhost:3000`
- Backend API: `http://localhost:8080`

Useful commands:

```bash
docker compose down
docker compose down -v
```

- `docker compose down` stops containers.
- `docker compose down -v` also removes local Docker volumes (database data and uploaded files).

## Verification Commands

Backend:

```bash
cd backend/pawnscan
./mvnw test
```

Web:

```bash
cd web
npm run lint
npm run test
npm run build
```

Mobile:

```powershell
cd mobile
.\gradlew.bat test
.\gradlew.bat assembleDebug
```

## Documentation

The software design document is available at `docs/SDD_PawnScan_Garbo.pdf`.
