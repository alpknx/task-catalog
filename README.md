# Task Catalog

REST service for task management built with Kotlin, Spring Boot WebFlux, and PostgreSQL.

## Prerequisites

- Java 17+
- PostgreSQL (for running the app)
- No PostgreSQL needed for tests (uses H2 in-memory)

## Run Tests

```bash
./gradlew test
```

Tests use an embedded H2 database — no external dependencies required.

## Set Up PostgreSQL

Create the database:

```bash
psql -U postgres -c "CREATE DATABASE task_catalog;"
```

Or via Docker:

```bash
docker run -d --name task-pg \
  -e POSTGRES_DB=task_catalog \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16
```

Flyway runs migrations automatically on startup.

## Run the Application

```bash
./gradlew bootRun
```

The server starts at `http://localhost:8080`.

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | /api/tasks | Create a task |
| GET | /api/tasks?page=0&size=10&status=NEW | List with pagination + optional filter |
| GET | /api/tasks/{id} | Get by ID |
| PATCH | /api/tasks/{id}/status | Update status |
| DELETE | /api/tasks/{id} | Delete a task |

Valid statuses: `NEW`, `IN_PROGRESS`, `DONE`, `CANCELLED`.

## Manual Testing

### Create a task

```bash
curl -s -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title": "My first task", "description": "Something to do"}' | jq
```

### List all tasks (paginated)

```bash
curl -s "http://localhost:8080/api/tasks?page=0&size=10" | jq
```

### Filter by status

```bash
curl -s "http://localhost:8080/api/tasks?status=NEW" | jq
```

### Get a task by ID

```bash
curl -s http://localhost:8080/api/tasks/1 | jq
```

### Update task status

```bash
curl -s -X PATCH http://localhost:8080/api/tasks/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "IN_PROGRESS"}' | jq
```

### Delete a task

```bash
curl -s -X DELETE http://localhost:8080/api/tasks/1 -w "\nHTTP %{http_code}\n"
```

Returns `204 No Content` on success.

### Test validation errors

Blank title returns `400 Bad Request`:

```bash
curl -s -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title": ""}' | jq
```

Title too short (min 3 characters):

```bash
curl -s -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title": "ab"}' | jq
```

Invalid status returns `400 Bad Request`:

```bash
curl -s -X PATCH http://localhost:8080/api/tasks/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "INVALID"}' | jq
```

### Test 404 Not Found

```bash
curl -s http://localhost:8080/api/tasks/999 | jq
```

## Configuration

Database connection is configured in `src/main/resources/application.yml`:

| Property | Default |
|---|---|
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/task_catalog` |
| `spring.datasource.username` | `postgres` |
| `spring.datasource.password` | `postgres` |

Override via environment variables:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://myhost:5432/mydb ./gradlew bootRun
```

## Project Structure

```
src/main/kotlin/com/example/taskcatalog/
├── controller/     TaskController (REST endpoints)
├── service/        TaskService interface + reactive implementation
├── repository/     JdbcClient-based repository with native SQL
├── model/          Task entity, TaskStatus enum
├── dto/            Request/response DTOs
├── exception/      TaskNotFoundException, GlobalExceptionHandler
```
