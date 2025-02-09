package com.github.ai.split.utils

import java.io.{PrintWriter, StringWriter}

extension (exception: Throwable)
  def stackTraceToString(): String = {
    val writer = new StringWriter()
    exception.printStackTrace(new PrintWriter(writer))
    writer.toString
  }
