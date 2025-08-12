package com.github.ai.split.entity

enum FileExtension {
  case CSV, HTML
}

object FileExtension {

  def fromString(name: String): Option[FileExtension] =
    values.find(value => value.toString == name)
}
