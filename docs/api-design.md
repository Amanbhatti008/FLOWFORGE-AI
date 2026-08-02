# API Design Guidelines

Our APIs follow standard REST principles and use OpenAPI 3.0 (Swagger) for documentation.

## Authentication
All endpoints under `/api/v1/workflows` and `/api/v1/users` require a Bearer token.
- `Access Token`: 15-minute expiration, used for all requests.
- `Refresh Token`: 7-day expiration, used strictly at `/api/v1/auth/refresh` to get a new access token.

## Standard Responses
Success (200 OK or 201 Created):
```json
{
  "id": "uuid",
  "name": "example",
  "status": "SUCCESS"
}
```

Error (4XX or 5XX):
```json
{
  "timestamp": "2026-08-02T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid DAG structure",
  "path": "/api/v1/workflows"
}
```

## Available Resources
- `/api/v1/auth/*`: Authentication operations
- `/api/v1/workflows/*`: CRUD for workflows
- `/api/v1/executions/*`: Trigger and view workflow executions
