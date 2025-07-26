package com.github.ai.split.client

import scala.util.Random
import java.util.Date

object Data {

  def newExpenseTitle(): String = s"Expense ${Date()}"
}
