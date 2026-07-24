# Backend

### Generate openapi.yaml file
```
sbt generateOpenApi
```

### Run backend app
```
sbt app/assembly
java -jar ./app/target/.../simple-split-backend.jar
```

### OpenAPI UI
OpenAPI UI is available here (the backend/app should be running):
```
https://127.0.0.1:8443/docs/openapi
```