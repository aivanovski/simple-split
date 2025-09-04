package com.github.ai.simplesplit.android.data.api.coverters

import com.github.ai.simplesplit.android.data.database.model.CurrencyEntity
import com.github.ai.simplesplit.android.model.Expense
import com.github.ai.simplesplit.android.model.Group
import com.github.ai.simplesplit.android.model.Member
import com.github.ai.simplesplit.android.model.Transaction
import com.github.ai.split.api.CurrencyDto
import com.github.ai.split.api.ExpenseDto
import com.github.ai.split.api.GroupDto
import com.github.ai.split.api.MemberDto
import com.github.ai.split.api.TransactionDto

fun List<CurrencyDto>.toCurrencies(): List<CurrencyEntity> {
    return this.map { dto -> dto.toCurrency() }
}

fun CurrencyDto.toCurrency(): CurrencyEntity =
    CurrencyEntity(
        isoCode = isoCode,
        name = name,
        symbol = symbol
    )

fun MemberDto.toMember(): Member =
    Member(
        uid = uid,
        name = name
    )

fun TransactionDto.toTransaction(): Transaction =
    Transaction(
        creditorUid = creditorUid,
        debtorUid = debtorUid,
        amount = amount
    )

fun ExpenseDto.toExpense(): Expense =
    Expense(
        uid = uid,
        title = title,
        description = description,
        amount = amount,
        currency = currency.toCurrency(),
        paidBy = paidBy.map { it.toMember() },
        splitBetween = splitBetween.map { it.toMember() },
        createdSeconds = created.timestampSeconds,
        modifiedSeconds = modified.timestampSeconds
    )

fun GroupDto.toGroup(): Group =
    Group(
        uid = uid,
        title = title,
        description = description,
        currency = currency.toCurrency(),
        members = members.map { it.toMember() },
        expenses = expenses.map { it.toExpense() },
        paybackTransactions = paybackTransactions.map { it.toTransaction() },
        createdSeconds = created.timestampSeconds,
        modifiedSeconds = modified.timestampSeconds
    )