package com.github.ai.split.utils

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import zio.http.Charsets

class AnsiStrippingEncoder extends PatternLayoutEncoder {

  // Regular expression to match ANSI escape sequences
  // Matches patterns like: \u001B[34m, \u001B[0m, [34m, [0m, etc.
  private val ansiPattern = "\\u001B\\[[;\\d]*m|\\[\\d+m".r

  override def encode(event: ILoggingEvent): Array[Byte] = {
    // Get the encoded message from the parent encoder
    val encoded = super.encode(event)

    // Convert to string, strip ANSI codes, and convert back to bytes
    val message = new String(encoded, Charsets.Utf8)
    val stripped = ansiPattern.replaceAllIn(message, "")
    stripped.getBytes(Charsets.Utf8)
  }
}
