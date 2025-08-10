package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.repository.{ExpenseRepository, GroupRepository}
import com.github.ai.split.entity.ExportFileContent
import com.github.ai.split.entity.db.GroupUid
import com.github.ai.split.entity.exception.DomainError
import zio.*
import zio.direct.*

class ExportGroupDataUseCase(
  private val groupRepository: GroupRepository,
  private val expenseRepository: ExpenseRepository
) {
  
  def exportDataToHtml(
    groupUid: GroupUid
  ): IO[DomainError, ExportFileContent] = {
    for {
      group <- groupRepository.getByUid(groupUid)
      expenses <- expenseRepository.getByGroupUid(groupUid)
    } yield {
      // Create a map from member UID to user name for quick lookup
      val memberUidToNameMap = group.members.map(member => member.entity.uid -> member.user.name).toMap

      // Generate HTML content
      val htmlContent = generateHtmlContent(group, expenses, memberUidToNameMap)

      ExportFileContent(
        fileName = group.entity.title + ".html",
        content = htmlContent
      )
    }
  }

  def exportDataToCsv(
    groupUid: GroupUid
  ): IO[DomainError, ExportFileContent] = {
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

      ExportFileContent(
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

  private def generateHtmlContent(
    group: com.github.ai.split.entity.GroupWithMembers,
    expenses: List[com.github.ai.split.entity.ExpenseWithRelations],
    memberUidToNameMap: Map[com.github.ai.split.entity.db.MemberUid, String]
  ): String = {
    val groupTitle = escapeHtml(group.entity.title)
    val membersList = group.members.map(member => s"<li>${escapeHtml(member.user.name)}</li>").mkString("\n        ")
    
    val expensesRows = expenses.map { expenseWithRelations =>
      val expense = expenseWithRelations.entity
      
      // Resolve paid by member names
      val paidByNames = expenseWithRelations.paidBy
        .map(_.memberUid)
        .map(memberUidToNameMap.getOrElse(_, "Unknown"))
        .mkString(", ")

      // Resolve split between member names
      val splitBetweenNames = if (expense.isSplitBetweenAll) {
        group.members.map(_.user.name).mkString(", ")
      } else {
        expenseWithRelations.splitBetween
          .map(_.memberUid)
          .map(memberUidToNameMap.getOrElse(_, "Unknown"))
          .mkString(", ")
      }

      s"""        <tr>
         |          <td>${escapeHtml(expense.title)}</td>
         |          <td>${escapeHtml(expense.description)}</td>
         |          <td class="amount">$${expense.amount}</td>
         |          <td>${escapeHtml(paidByNames)}</td>
         |          <td>${escapeHtml(splitBetweenNames)}</td>
         |        </tr>""".stripMargin
    }.mkString("\n")

    val totalAmount = expenses.map(_.entity.amount).sum

    s"""<!DOCTYPE html>
       |<html lang="en">
       |<head>
       |    <meta charset="UTF-8">
       |    <meta name="viewport" content="width=device-width, initial-scale=1.0">
       |    <title>$groupTitle - Expense Report</title>
       |    <style>
       |        body {
       |            font-family: Arial, sans-serif;
       |            max-width: 1200px;
       |            margin: 0 auto;
       |            padding: 20px;
       |            background-color: #f5f5f5;
       |        }
       |        .container {
       |            background-color: white;
       |            border-radius: 8px;
       |            padding: 30px;
       |            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
       |        }
       |        h1 {
       |            color: #333;
       |            border-bottom: 3px solid #007bff;
       |            padding-bottom: 10px;
       |        }
       |        .summary {
       |            background-color: #f8f9fa;
       |            padding: 20px;
       |            border-radius: 5px;
       |            margin: 20px 0;
       |        }
       |        .members {
       |            margin: 20px 0;
       |        }
       |        .members ul {
       |            list-style-type: none;
       |            padding: 0;
       |        }
       |        .members li {
       |            background-color: #e9ecef;
       |            padding: 8px 15px;
       |            margin: 5px 0;
       |            border-radius: 4px;
       |            display: inline-block;
       |            margin-right: 10px;
       |        }
       |        table {
       |            width: 100%;
       |            border-collapse: collapse;
       |            margin-top: 20px;
       |        }
       |        th, td {
       |            padding: 12px;
       |            text-align: left;
       |            border-bottom: 1px solid #ddd;
       |        }
       |        th {
       |            background-color: #007bff;
       |            color: white;
       |            font-weight: bold;
       |        }
       |        tr:hover {
       |            background-color: #f5f5f5;
       |        }
       |        .amount {
       |            text-align: right;
       |            font-weight: bold;
       |        }
       |        .total {
       |            background-color: #28a745;
       |            color: white;
       |            font-weight: bold;
       |        }
       |        .total td {
       |            padding: 15px 12px;
       |        }
       |        .no-expenses {
       |            text-align: center;
       |            color: #666;
       |            font-style: italic;
       |            padding: 40px;
       |        }
       |    </style>
       |</head>
       |<body>
       |    <div class="container">
       |        <h1>$groupTitle</h1>
       |        
       |        <div class="summary">
       |            <h3>Summary</h3>
       |            <p><strong>Total Expenses:</strong> ${expenses.length}</p>
       |            <p><strong>Total Amount:</strong> $$totalAmount</p>
       |        </div>
       |
       |        <div class="members">
       |            <h3>Group Members</h3>
       |            <ul>
       |        $membersList
       |            </ul>
       |        </div>
       |
       |        <h3>Expenses</h3>
       |        ${if (expenses.nonEmpty) {
         s"""<table>
            |            <thead>
            |                <tr>
            |                    <th>Title</th>
            |                    <th>Description</th>
            |                    <th>Amount</th>
            |                    <th>Paid By</th>
            |                    <th>Split Between</th>
            |                </tr>
            |            </thead>
            |            <tbody>
            |$expensesRows
            |            </tbody>
            |            <tfoot>
            |                <tr class="total">
            |                    <td colspan="2"><strong>Total</strong></td>
            |                    <td class="amount"><strong>$$totalAmount</strong></td>
            |                    <td colspan="2"></td>
            |                </tr>
            |            </tfoot>
            |        </table>""".stripMargin
       } else {
         """<div class="no-expenses">No expenses found for this group.</div>"""
       }}
       |    </div>
       |</body>
       |</html>""".stripMargin
  }

  private def escapeHtml(text: String): String = {
    text
      .replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("\"", "&quot;")
      .replace("'", "&#x27;")
  }
}
