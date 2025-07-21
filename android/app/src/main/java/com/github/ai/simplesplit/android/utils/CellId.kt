package com.github.ai.simplesplit.android.utils

import com.github.ai.simplesplit.android.utils.CellIdPayload.IntPayload
import com.github.ai.simplesplit.android.utils.CellIdPayload.StringPayload

data class CellId(
    val prefix: String,
    val payload: CellIdPayload
)

sealed interface CellIdPayload {
    data class IntPayload(val intValue: Int) : CellIdPayload
    data class StringPayload(val text: String) : CellIdPayload
}

fun CellIdPayload.getStringOrNull(): String? = (this as? StringPayload)?.text

fun CellIdPayload.getIntOrNull(): Int? = (this as? IntPayload)?.intValue

fun CellId.format(): String {
    val (payloadType, payloadValue) = when (payload) {
        is CellIdPayload.IntPayload -> "Int" to payload.intValue.toString()
        is CellIdPayload.StringPayload -> "String" to payload.text
    }

    return "CELL_ID($payloadType):$prefix:$payloadValue"
}

fun String.parseCellId(): CellId? {
    val regex = Regex("CELL_ID\\((Int|String)\\):(.*):(.*)$")
    val matchResult = regex.find(this) ?: return null

    val (_, payloadType, prefix, value) = matchResult.groupValues

    val payload = when (payloadType) {
        "Int" -> {
            val intValue = value.toIntOrNull() ?: return null
            CellIdPayload.IntPayload(intValue)
        }

        "String" -> CellIdPayload.StringPayload(value)
        else -> return null
    }

    return CellId(prefix, payload)
}