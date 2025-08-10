package com.github.ai.simplesplit.android.presentation.screens.checkoutGroup

import arrow.core.Either
import arrow.core.raise.either
import com.github.ai.simplesplit.android.data.repository.GroupCredentialsRepository
import com.github.ai.simplesplit.android.data.repository.GroupRepository
import com.github.ai.simplesplit.android.domain.usecase.ParseGroupUrlUseCase
import com.github.ai.simplesplit.android.model.exception.AppException
import com.github.ai.split.api.GroupDto

class CheckoutGroupInteractor(
    private val groupRepository: GroupRepository,
    private val parseGroupUrlUseCase: ParseGroupUrlUseCase,
    private val credentialsRepository: GroupCredentialsRepository
) {

    suspend fun addGroup(url: String): Either<AppException, GroupDto> =
        either {
            val credentials = parseGroupUrlUseCase.parseUrl(url).bind()

            val group = groupRepository.getGroup(
                uid = credentials.groupUid,
                password = credentials.password
            ).bind()

            credentialsRepository.add(credentials)

            group
        }
}