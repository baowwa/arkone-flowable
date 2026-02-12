# Comprehensive REST API Documentation

## Introduction
This document provides comprehensive information about the REST API, its endpoints, request and response schemas, and usage examples.

## Endpoints

| Method | Endpoint               | Description                          |
|--------|------------------------|--------------------------------------|
| GET    | /api/v1/resource       | Retrieve a list of resources       |
| POST   | /api/v1/resource       | Create a new resource              |
| GET    | /api/v1/resource/{id}  | Retrieve a specific resource       |
| PUT    | /api/v1/resource/{id}  | Update a specific resource         |
| DELETE | /api/v1/resource/{id}  | Delete a specific resource         |

## Request/Response Schemas

### Request Schema for Creating a Resource
```json
{
    "name": "string",
    "description": "string",
    "created_at": "string (ISO 8601 date format)"
}
```

### Response Schema for Resource
```json
{
    "id": "string",
    "name": "string",
    "description": "string",
    "created_at": "string (ISO 8601 date format)",
    "updated_at": "string (ISO 8601 date format)"
}
```

## Usage Examples

### Example: Create a Resource
```bash
curl -X POST \
     -H "Content-Type: application/json" \
     -d '{"name":"Sample Resource","description":"This is a sample."}' \
     https://api.example.com/api/v1/resource
```

### Example: Get All Resources
```bash
curl -X GET https://api.example.com/api/v1/resource
```

---

This documentation will be updated as the API evolves.