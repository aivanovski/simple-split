package com.github.ai.split.entity.db

case class Timestamp(
  seconds: Long
) extends AnyVal

object Timestamp {

  def now() =
    Timestamp(seconds = System.currentTimeMillis() / 1000L)
}
