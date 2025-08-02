package com.github.ai.split.codegen.model

case class KotlinType(
  packageName: String,
  imports: List[String],
  typeName: String,
  fields: List[Field]
)
