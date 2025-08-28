package com.github.ai.simplesplit.android.presentation.screens.groupEditor.model

import com.github.ai.simplesplit.android.data.database.model.CurrencyEntity
import com.github.ai.split.api.GroupDto

data class GroupEditorData(
    val currencies: List<CurrencyEntity>,
    val group: GroupDto
)