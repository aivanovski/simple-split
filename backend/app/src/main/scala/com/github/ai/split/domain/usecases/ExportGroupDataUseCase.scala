package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.repository.{ExpenseRepository, GroupRepository}
import com.github.ai.split.entity.ExportContent
import com.github.ai.split.entity.db.GroupUid
import com.github.ai.split.entity.exception.DomainError
import zio.*
import zio.direct.*

class ExportGroupDataUseCase(
  private val groupRepository: GroupRepository,
  private val expenseRepository: ExpenseRepository
) {

  def exportDataToCsv(
    groupUid: GroupUid
  ): IO[DomainError, ExportContent] = {
    defer {
      val group = groupRepository.getByUid(groupUid).run
      val expenses = expenseRepository.getByGroupUid(groupUid).run

      // Create a map from member UID to user name for quick lookup
      val memberUidToNameMap = group.members.map(member => member.entity.uid -> member.user.name).toMap

      // CSV header
      val header = "Title,Description,Amount,Paid By,Split Between"

      // Convert expenses to CSV rows
      val csvRows = expenses.map { expenseWithRelations =>
        val expense = expenseWithRelations.entity

        // Resolve paid by member names
        val paidByNames = expenseWithRelations.paidBy
          .map(_.memberUid)
          .map(memberUidToNameMap.getOrElse(_, "Unknown"))
          .mkString("; ")

        // Resolve split between member names
        val splitBetweenNames = if (expense.isSplitBetweenAll) {
          group.members.map(_.user.name).mkString("; ")
        } else {
          expenseWithRelations.splitBetween
            .map(_.memberUid)
            .map(memberUidToNameMap.getOrElse(_, "Unknown"))
            .mkString("; ")
        }

        // Format CSV row with proper escaping
        val title = escapeCsvField(expense.title)
        val description = escapeCsvField(expense.description)
        val amount = expense.amount.toString
        val paidBy = escapeCsvField(paidByNames)
        val splitBetween = escapeCsvField(splitBetweenNames)

        s"$title,$description,$amount,$paidBy,$splitBetween"
      }

      // Combine header and rows
      val content = (header :: csvRows).mkString("\n")

      ExportContent(
        fileName = group.entity.title + ".csv",
        content = content
      )
    }
  }

  private def escapeCsvField(field: String): String = {
    if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
      "\"" + field.replace("\"", "\"\"") + "\""
    } else {
      field
    }
  }
}
