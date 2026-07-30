# Generated backend API models

The Rust DTOs in `src/models` are generated from the backend's OpenAPI schema.
Regenerate them from the repository root with:

```sh
openapi-generator generate \
  -g rust \
  -i backend/openapi-schema/openapi.yaml \
  -o frontend/backend-api \
  --global-property models,modelDocs=false,modelTests=false \
  --additional-properties avoidBoxedModels=true
```

The generator does not emit `src/models/mod.rs` in model-only mode. If schemas
are added or removed, update that module's declarations and re-exports too.
