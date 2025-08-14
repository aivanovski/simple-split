package com.github.ai.split.client.utils

import zio.*
import scala.collection.mutable

def parseOptionsIntoMap(args: List[String]): IO[String, Map[String, String]] = {
  // TODO: fix
  val queue = mutable.Queue(args: _*)
  val result = mutable.Map.empty[String, String]

  while (queue.nonEmpty) {
    val token = queue.dequeue()

    if (!token.startsWith("--")) {
      return ZIO.fail(s"Unexpected argument: $token")
    }

    val key = token.drop(2)
    if (queue.isEmpty) {
      return ZIO.fail(s"Missing value for option '$key'")
    }

    val value = queue.dequeue()
    if (value.startsWith("--")) {
      return ZIO.fail(s"Expected value for option '$key', but got another option: $value")
    }

    result.update(key, value)
  }

  ZIO.succeed(result.toMap)
}
