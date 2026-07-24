# User Mail Sender MS

Event-driven microservices for user registration and welcome-email delivery.

When a user registers, the **user** service persists the account, issues a JWT on login, and publishes a welcome message to RabbitMQ. The **email** service consumes that message, stores the email record, and sends it over SMTP.

```
┌────────────┐   POST /auth/register    ┌──────────────┐
│   Client   │ ───────────────────────► │ user :8082   │
└────────────┘                          └──────┬───────┘
                                               │ publish
                                               │ exchange: app-exchange
                                               │ routing: email.welcome
                                               ▼
                                        ┌──────────────┐
                                        │   RabbitMQ   │
                                        └──────┬───────┘
                                               │ consume
                                               │ queue: email-queue
                                               ▼
                                        ┌──────────────┐     SMTP
                                        │ email :8081  │ ──────────► inbox
                                        └──────────────┘
```

---

## Tech stack

| Layer | Technology |
|-------|------------|
| Runtime | Java 17 |
| Framework | Spring Boot 3.4.2 |
| API | Spring Web + Validation |
| Security | Spring Security + Auth0 java-jwt |
| Messaging | Spring AMQP (RabbitMQ) |
| Persistence | Spring Data JPA + PostgreSQL 16 |
| Migrations | Flyway |
| Mail | Spring Mail (SMTP) |
| Tests | JUnit 5 + Mockito |
| Containers | Docker Compose (databases) |

---

## Project structure

```
user-mail-sender-ms/
├── docker-compose.yml          # PostgreSQL for both services
├── user/                       # Auth & registration (port 8082)
│   ├── .env.example
│   ├── pom.xml
│   └── src/main/java/dev/user/
│       ├── controller/         # REST API + exception handling
│       ├── service/            # Register / login
│       ├── security/           # JWT filter & token service
│       ├── producer/           # RabbitMQ publisher
│       ├── repository/
│       ├── domain/
│       └── dto/
└── email/                      # Email consumer & SMTP (port 8081)
    ├── .env.example
    ├── pom.xml
    └── src/main/java/dev/email/
        ├── consumer/           # RabbitMQ listener
        ├── service/            # Persist + send mail
        ├── repository/
        ├── domain/
        └── dto/
```

Each module is an independent Maven project (no parent aggregator).

---

## Prerequisites

- **JDK 17+**
- **Maven 3.9+** (or use the included `mvnw` / `mvnw.cmd`)
- **Docker** & Docker Compose (for PostgreSQL)
- **RabbitMQ** broker (CloudAMQP, local Docker, or similar)
- **SMTP** credentials (e.g. Gmail App Password)

---

## Architecture overview

### User service (`user`)

Responsibilities:

- Register users (`username`, `email`, `password`) with role `USER`
- Hash passwords (BCrypt)
- Authenticate by username **or** email and return a JWT
- Publish a welcome `EmailDto` to RabbitMQ after successful registration

| Endpoint | Method | Auth | Status |
|----------|--------|------|--------|
| `/auth/register` | `POST` | Public | `201 Created` |
| `/auth/login` | `POST` | Public | `202 Accepted` |

JWT claims: subject = email, `role`, issuer `auth-api`, expiry from `api.security.token.expiration-hours`.

### Email service (`email`)

Responsibilities:

- Listen on `email-queue`
- Persist emails with status `PENDING` → `SENT` or `FAILED`
- Send HTML mail via SMTP

RabbitMQ topology (declared by the email service):

| Resource | Name |
|----------|------|
| Exchange | `app-exchange` (topic) |
| Queue | `email-queue` |
| Binding | `email.#` |
| Publish key (user service) | `email.welcome` |

---

## Getting started

### 1. Clone the repository

```bash
git clone https://github.com/<your-org>/user-mail-sender-ms.git
cd user-mail-sender-ms
```

### 2. Start the databases

Create a root `.env` (or export variables) for Compose:

```env
DB_NAME_USER=ms_user
DB_USER_USER=user
DB_PASSWORD_USER=change-me

DB_NAME_MAIL=ms_email
DB_USER_MAIL=user
DB_PASSWORD_MAIL=change-me
```

Map host ports in `docker-compose.yml` if they are empty, for example:

```yaml
# db-user
ports:
  - "5432:5432"

# db-mail
ports:
  - "5433:5432"
```

Then start:

```bash
docker compose up -d
```

### 3. Configure environment variables

Copy the examples and fill in real values:

```bash
cp user/.env.example user/.env
cp email/.env.example email/.env
```

Both `application.yml` files read variables from the environment. Typical values:

#### User service

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_URL` | JDBC URL | `jdbc:postgresql://localhost:5432/ms_user` |
| `DB_USER` | DB username | `user` |
| `DB_PASSWORD` | DB password | `change-me` |
| `SECRET` | JWT HMAC secret | long random string |
| `MQ_ADDRESS` | RabbitMQ host:port | `your-host:5671` |
| `MQ_USER` | RabbitMQ user / vhost | `your-user` |
| `MQ_PASSWORD` | RabbitMQ password | `your-password` |

#### Email service

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_URL` | JDBC URL | `jdbc:postgresql://localhost:5433/ms_email` |
| `DB_USER` | DB username | `user` |
| `DB_PASSWORD` | DB password | `change-me` |
| `MQ_ADDRESS` | RabbitMQ host:port | `your-host:5671` |
| `MQ_USER` | RabbitMQ user / vhost | `your-user` |
| `MQ_PASSWORD` | RabbitMQ password | `your-password` |
| `SMTP_HOST` | SMTP host | `smtp.gmail.com` |
| `SMTP_PORT` | SMTP port | `587` |
| `SMTP_USER` | SMTP username | `you@gmail.com` |
| `SMTP_PASSWORD` | SMTP password / app password | `xxxx` |

> **Note:** `application.yml` sets `spring.rabbitmq.ssl.enabled: true` and `virtual-host: ${MQ_USER}`. Adjust these if you use a local broker without SSL or a different vhost.

### 4. Run the services

**User** (port `8082`):

```bash
cd user
./mvnw spring-boot:run
# Windows: mvnw.cmd spring-boot:run
```

**Email** (port `8081`):

```bash
cd email
./mvnw spring-boot:run
# Windows: mvnw.cmd spring-boot:run
```

Flyway runs migrations on startup:

- User: `users_tb` (+ `role` column)
- Email: `emails`

---

## API examples

### Register

```http
POST http://localhost:8082/auth/register
Content-Type: application/json

{
  "username": "joao",
  "email": "joao@email.com",
  "password": "senha123"
}
```

**Success (`201`):**

```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "username": "joao",
  "email": "joao@email.com"
}
```

**Conflict (`409`)** — username or email already exists:

```text
Username already exists
```

### Login

```http
POST http://localhost:8082/auth/login
Content-Type: application/json

{
  "login": "joao@email.com",
  "password": "senha123"
}
```

`login` accepts either username or email.

**Success (`202`):** JWT string in the response body.

**Unauthorized (`401`):**

```text
Invalid credentials
```

### Authenticated requests

For protected routes, send:

```http
Authorization: Bearer <jwt>
```

The filter reads the `role` claim and sets authority `ROLE_USER` or `ROLE_ADMIN`.

---

## Message flow (welcome email)

1. Client calls `POST /auth/register`.
2. User service saves the user and calls `UserProducer.publishEmailMessage`.
3. Message is published to `app-exchange` with routing key `email.welcome`.
4. Email service consumes from `email-queue`, maps the DTO to `EmailModel`, and calls `MailService.sendEmail`.
5. Status is stored as `PENDING`, then updated to `SENT` or `FAILED`.

Email statuses: `PENDING` | `SENT` | `FAILED` | `DELIVERED`.

---

## Testing

Unit tests use **JUnit 5** and **Mockito** (no live DB/RabbitMQ/SMTP required).

```bash
# User module
cd user && ./mvnw test

# Email module
cd email && ./mvnw test
```

Coverage includes:

| Module | Tests |
|--------|--------|
| User | `UserService`, `TokenService`, `UserProducer`, `UserController` |
| Email | `MailService`, `EmailConsumer` |

Full `@SpringBootTest` context loads are disabled by default because they need real infrastructure.

---

## Database schemas

### `users_tb` (user service)

| Column | Type | Notes |
|--------|------|--------|
| `id` | UUID | PK |
| `username` | VARCHAR(50) | unique |
| `email` | VARCHAR(100) | unique |
| `password` | VARCHAR(255) | BCrypt hash |
| `role` | VARCHAR(20) | `USER` / `ADMIN` |

### `emails` (email service)

| Column | Type | Notes |
|--------|------|--------|
| `email_id` | UUID | PK |
| `user_id` | UUID | registrant |
| `email_from` | VARCHAR(255) | SMTP username |
| `email_to` | VARCHAR(255) | recipient |
| `email_subject` | VARCHAR(255) | |
| `email_body` | TEXT | HTML supported |
| `email_status` | VARCHAR(50) | enum name |

---

## Configuration reference

| Service | Port | Key config |
|---------|------|------------|
| User | `8082` | `api.security.token.secret`, RabbitMQ, PostgreSQL |
| Email | `8081` | SMTP, RabbitMQ, PostgreSQL |
| Postgres (user) | host mapped from Compose | Flyway validate |
| Postgres (email) | host mapped from Compose | Flyway migrate |

---

## Troubleshooting

| Symptom | Likely cause |
|---------|----------------|
| App fails on startup with missing placeholders | Env vars (`DB_URL`, `SECRET`, `MQ_*`, `SMTP_*`) not set |
| RabbitMQ connection / SSL errors | Broker without TLS — set `spring.rabbitmq.ssl.enabled` to `false` for local |
| Emails stay `FAILED` | Invalid SMTP credentials or empty `emailTo` |
| `409` on register | Username or email already in `users_tb` |
| JWT always rejected | Wrong `SECRET` or expired token (`expiration-hours`) |

---

## License

No license file is present in this repository. Add one if you intend to distribute the project.
