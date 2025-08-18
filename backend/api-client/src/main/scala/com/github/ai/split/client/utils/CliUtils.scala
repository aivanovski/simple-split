package com.github.ai.split.client.utils

import zio.*
import scala.collection.mutable

def parseOptionsIntoMap(
  args: List[String],
  requiredArguments: Set[String]
): IO[String, Map[String, String]] = {
  val queue = mutable.Queue(args: _*)
  val result = mutable.Map.empty[String, String]

  while (queue.nonEmpty) {
    val token = queue.dequeue()

    if (!token.startsWith("--")) {
      return ZIO.fail(s"Unexpected argument: $token")
    }
    
    if (!requiredArguments.contains(token)) {
      return ZIO.fail(s"Unexpected argument: $token")
    }

    if (queue.isEmpty) {
      return ZIO.fail(s"Missing value for option '$token'")
    }

    val value = queue.dequeue()
    if (value.startsWith("--")) {
      return ZIO.fail(s"Expected value for option '$token', but got another option: $value")
    }

    result.update(token, value)
  }

  ZIO.succeed(result.toMap)
}
