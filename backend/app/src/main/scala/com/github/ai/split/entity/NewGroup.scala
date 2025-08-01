package com.github.ai.split.entity

case class NewGroup(
  password: String,
  title: String,
  description: String,
  members: List[NewUser],
  expenses: List[NewExpense]
)