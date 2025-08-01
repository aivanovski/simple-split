package com.github.ai.simplesplit.android.presentation.screens.groupEditor

import arrow.core.Either
import arrow.core.raise.either
import com.github.ai.simplesplit.android.data.repository.GroupCredentialsRepository
import com.github.ai.simplesplit.android.data.repository.GroupRepository
import com.github.ai.simplesplit.android.data.repository.MemberRepository
import com.github.ai.simplesplit.android.model.db.GroupCredentials
import com.github.ai.simplesplit.android.model.exception.AppException
import com.github.ai.split.api.GroupDto
import com.github.ai.split.api.UserNameDto
import com.github.ai.split.api.request.PostGroupRequest
import com.github.ai.split.api.request.PostMemberRequest
import com.github.ai.split.api.request.PutGroupRequest

class GroupEditorInteractor(
    private val groupRepository: GroupRepository,
    private val memberRepository: MemberRepository,
    private val credentialsRepository: GroupCredentialsRepository
) {

    suspend fun loadGroup(
        uid: String,
        password: String
    ): Either<AppException, GroupDto> = groupRepository.getGroup(uid, password)

    suspend fun updateGroup(
        credentials: GroupCredentials,
        newTitle: String?,
        newPassword: String?,
        membersToRemove: List<String>,
        membersToAdd: List<String>
    ): Either<AppException, GroupDto> =
        either {
            if (membersToAdd.isNotEmpty()) {
                for (member in membersToAdd) {
                    memberRepository.createMember(
                        password = credentials.password,
                        request = PostMemberRequest(
                            groupUid = credentials.groupUid,
                            name = membersToAdd.first()
                        )
                    ).bind()
                }
            }

            if (membersToRemove.isNotEmpty()) {
                for (member in membersToRemove) {
                    memberRepository.removeMember(
                        memberUid = member,
                        password = credentials.password
                    ).bind()
                }
            }

            val group = if (newTitle != null ||
                newPassword != null
            ) {
                groupRepository.updateGroup(
                    password = credentials.password,
                    uid = credentials.groupUid,
                    request = PutGroupRequest(
                        title = newTitle,
                        password = newPassword,
                        description = null,
                        members = null
                    )
                ).bind().group
            } else {
                groupRepository.getGroup(
                    uid = credentials.groupUid,
                    password = credentials.password
                ).bind()
            }

            group
        }

    suspend fun createGroup(
        password: String,
        title: String,
        members: List<String>
    ): Either<AppException, GroupDto> =
        either {
            val request = PostGroupRequest(
                password = password,
                title = title,
                description = null,
                members = members.map { UserNameDto(name = it) },
                expenses = null
            )

            val response = groupRepository.createGroup(request).bind()

            credentialsRepository.add(
                GroupCredentials(
                    groupUid = response.group.uid,
                    password = password
                )
            )

            response.group
        }
}