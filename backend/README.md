# Backend

### Generate openapi.yaml file
```
sbt generateOpenApi
```

### Run backend app
Copy `.env.example` to `.env` and adjust the values, then run:
```
sbt app/assembly
java -jar ./app/target/.../simple-split-backend.jar
```

The backend reads all startup configuration from `.env` (or from environment
variables with the same names).

### OpenAPI UI
OpenAPI UI is available here (the backend/app should be running):
```
https://127.0.0.1:8443/docs/openapi
```
