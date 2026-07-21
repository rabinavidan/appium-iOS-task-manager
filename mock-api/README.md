# Mock API

This is a lightweight mock backend for the Task Manager project. It is intentionally dependency-free and runs with Node.js only.

## Run

```sh
MOCK_API_PORT=8081 node server.js
```

## Endpoints

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/health` | Service health check |
| `GET` | `/tasks` | List all tasks |
| `POST` | `/tasks` | Create a task |
| `GET` | `/tasks/{id}` | Get one task |
| `PATCH` | `/tasks/{id}` | Update completion, notes, or priority |
| `DELETE` | `/tasks/{id}` | Delete one task |
| `DELETE` | `/tasks` | Clear all tasks |

## Create Task Example

```sh
curl -X POST http://127.0.0.1:8081/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"API task","notes":"Created from mock API","priority":"High"}'
```

