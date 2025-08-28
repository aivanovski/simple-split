package com.github.ai.simplesplit.android.presentation.screens.groups.model

import com.github.ai.simplesplit.android.data.database.model.CurrencyEntity
import com.github.ai.simplesplit.android.data.database.model.GroupCredentials
import com.github.ai.split.api.GroupDto

data class GroupsData(
    val groups: List<GroupDto>,
    val requestedCredentials: List<GroupCredentials>,
    val currencies: List<CurrencyEntity>
)