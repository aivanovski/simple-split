package com.github.ai.split.data

import com.github.ai.split.entity.exception.{DomainError, JsonDeserializationError, ParsingError}
import com.github.ai.split.utils.some
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import zio.{IO, Task, ZIO}

class JsonSerializer {

  private val gson = GsonBuilder().setPrettyPrinting().create()

  def serialize(data: Any): String = {
    gson.toJson(data)
  }

  def deserializer[T](
    data: Task[String],
    typeOf: Class[T]
  ): IO[ParsingError, T] = {
    for {
      json <- data.mapError(error => ParsingError(message = "Unable to read json data"))
      result <- ZIO
        .attempt {
          gson.fromJson(json, TypeToken.get(typeOf))
        }
        .mapError(error => JsonDeserializationError(typeOf = typeOf, cause = error.some))
    } yield result
  }
}
