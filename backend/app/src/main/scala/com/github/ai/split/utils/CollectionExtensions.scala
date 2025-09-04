package com.github.ai.split.utils

import scala.jdk.CollectionConverters.*

extension [T](list: List[T]) {
  def toJavaList(): java.util.List[T] = list.asJava
}

extension [T](javaList: java.util.List[T]) {
  def toScalaList(): List[T] = javaList.asScala.toList
}
