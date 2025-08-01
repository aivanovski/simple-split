package com.github.ai.split.entity.db

import java.util.UUID
import io.getquill.MappedEncoding

opaque type UserUid = UUID
opaque type GroupUid = UUID
opaque type MemberUid = UUID
opaque type ExpenseUid = UUID

object UserUid {
  def apply(uid: UUID): UserUid = uid
  
  implicit val encodeUserUid: MappedEncoding[UserUid, UUID] = MappedEncoding[UserUid, UUID](identity)
  implicit val decodeUserUid: MappedEncoding[UUID, UserUid] = MappedEncoding[UUID, UserUid](UserUid(_))
}

object GroupUid {
  def apply(uid: UUID): GroupUid = uid
  
  implicit val encodeGroupUid: MappedEncoding[GroupUid, UUID] = MappedEncoding[GroupUid, UUID](identity)
  implicit val decodeGroupUid: MappedEncoding[UUID, GroupUid] = MappedEncoding[UUID, GroupUid](GroupUid(_))
}

object MemberUid {
  def apply(uid: UUID): MemberUid = uid
  
  implicit val encodeMemberUid: MappedEncoding[MemberUid, UUID] = MappedEncoding[MemberUid, UUID](identity)
  implicit val decodeMemberUid: MappedEncoding[UUID, MemberUid] = MappedEncoding[UUID, MemberUid](MemberUid(_))
}

object ExpenseUid {
  def apply(uid: UUID): ExpenseUid = uid
  
  implicit val encodeExpenseUid: MappedEncoding[ExpenseUid, UUID] = MappedEncoding[ExpenseUid, UUID](identity)
  implicit val decodeExpenseUid: MappedEncoding[UUID, ExpenseUid] = MappedEncoding[UUID, ExpenseUid](ExpenseUid(_))
}
