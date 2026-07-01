# Task API

A REST API for managing tasks (create, read, update, delete, and filter), built with Spring Boot 4.1 and Java 25.

## Quick start

```bash
docker compose up -d --build
```

This builds the app image and starts it alongside a Postgres 18 container. The API is then available at `http://localhost:8080`.

## Tech stack

- Java 25, Spring Boot 4.1 (Web, Data JPA, Validation, Actuator)
- Postgres
- Lombok
- JUnit 5, Mockito, AssertJ

## Domain model

A `Task` has:

| Field         | Type                                | Notes                        |
|---------------|-------------------------------------|-------------------------------|
| `id`          | UUID                                | generated                     |
| `title`       | String                              | required, min 3 characters    |
| `description` | String                              | optional                      |
| `status`      | enum: `OPEN`, `IN_PROGRESS`, `DONE` | required                      |
| `priority`    | enum: `LOW`, `MEDIUM`, `HIGH`       | required                      |
| `createdAt`   | Instant                             | set automatically on create   |
| `updatedAt`   | Instant                             | set automatically on update   |

## API reference

Base path: `/tasks`

| Method | Path           | Description                                                          | Success                  | Failure                  |
|--------|----------------|-----------------------------------------------------------------------|---------------------------|----------------------------|
| POST   | `/tasks`       | Create a task                                                        | `201` + `Location` header | `400` (validation)         |
| GET    | `/tasks`       | List tasks, optional `?status=` / `?priority=` filters (combinable)  | `200`                     | -                          |
| GET    | `/tasks/{id}`  | Get a task by id                                                     | `200`                     | `404`                      |
| PUT    | `/tasks/{id}`  | Full update of a task                                                | `200`                     | `400` (validation) / `404` |
| DELETE | `/tasks/{id}`  | Delete a task                                                        | `204`                     | `404`                      |

### Example requests

```bash
# Create
curl -i -X POST http://localhost:8080/tasks -H "Content-Type: application/json" -d '{"title":"Write report","description":"Q3 summary","status":"OPEN","priority":"HIGH"}'

# List / filter
curl -s http://localhost:8080/tasks
curl -s "http://localhost:8080/tasks?status=OPEN"
curl -s "http://localhost:8080/tasks?priority=HIGH"
curl -s "http://localhost:8080/tasks?status=OPEN&priority=HIGH"

# Get / update / delete one (replace <id> with an actual task id)
curl -i http://localhost:8080/tasks/<id>
curl -i -X PUT http://localhost:8080/tasks/<id> -H "Content-Type: application/json" -d '{"title":"Write report","description":"Q3 summary - done","status":"DONE","priority":"HIGH"}'
curl -i -X DELETE http://localhost:8080/tasks/<id>
```

### Error responses

Errors follow [RFC 7807](https://datatracker.ietf.org/doc/html/rfc7807) problem details:

```json
{
  "type": "about:blank",
  "title": "Task not found",
  "status": 404,
  "detail": "Task not found with id: 00000000-0000-0000-0000-000000000000",
  "instance": "/tasks/00000000-0000-0000-0000-000000000000",
  "timestamp": "2026-07-01T18:09:52.617501783Z"
}
```

Validation failures (`400`) additionally include an `errors` array of `field: message` strings.

## Architecture

Standard layered structure — controller → service → repository — with the entity never exposed directly over HTTP; requests/responses go through dedicated `TaskRequest`/`TaskResponse` DTOs.

## Running locally without Docker

Requires Java 25 and a running Postgres instance.

```bash
docker compose up -d postgres
./mvnw spring-boot:run
```

The app reads `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` env vars, defaulting to the Postgres service defined in `docker-compose.yaml` if unset.

## Running tests

```bash
./mvnw test
```

Covers the repository (filtering, persistence), service (all branches, Mockito), and controller (MockMvc, validation and status codes) layers.
