package com.github.ai.split.utils

extension (str: String)
  def removeSuffixAfter(symbol: String): String = {
    val lastIndex = str.lastIndexOf(symbol)
    if lastIndex >= 0 then str.substring(0, lastIndex) else str
  }
