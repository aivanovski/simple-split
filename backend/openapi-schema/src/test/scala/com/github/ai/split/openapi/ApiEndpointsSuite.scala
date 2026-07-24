package com.github.ai.split.openapi

class ApiEndpointsSuite extends munit.FunSuite {
  test("OpenAPI schema initializes with CSV export endpoint") {
    val schema = ApiEndpoints.openApi.toJson

    assert(schema.contains("/export/{groupIdAndExtension}"))
    assert(schema.contains("text/csv"))
    assert(schema.contains("/signup"))
    assert(schema.contains("/login"))
    assert(schema.contains("/auth/refresh"))
  }

  test("generated schema can be represented as YAML") {
    GenerateOpenApi.main(Array("target/test-openapi.yaml"))

    val schema = java.nio.file.Files.readString(java.nio.file.Path.of("target/test-openapi.yaml"))
    assert(schema.contains("openapi: 3.1.0"))
    assert(schema.contains("Simple Split API"))
  }
}
