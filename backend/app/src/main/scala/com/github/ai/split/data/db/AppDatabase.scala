package com.github.ai.split.data.db

import com.github.ai.split.entity.db.{
  CurrencyEntity,
  ExpenseEntity,
  ExpenseUid,
  GroupEntity,
  GroupMemberEntity,
  GroupUid,
  MemberUid,
  PaidByEntity,
  SplitBetweenEntity,
  Timestamp,
  UserEntity,
  UserUid
}
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.toDomainError
import slick.jdbc.SQLiteProfile.api.*
import slick.lifted.ProvenShape
import zio.{IO, ZIO}
import zio.direct.*

import java.util.UUID

class AppDatabase(
  val context: Database
) {

  val CurrencyTable = TableQuery[CurrencyEntityTable]
  val UserTable = TableQuery[UserEntityTable]
  val GroupMemberTable = TableQuery[GroupMemberEntityTable]
  val PaidByTable = TableQuery[PaidByEntityTable]
  val SplitBetweenTable = TableQuery[SplitBetweenEntityTable]
  val ExpenseTable = TableQuery[ExpenseEntityTable]
  val GroupTable = TableQuery[GroupEntityTable]

  def initialize(): IO[DomainError, Unit] =
    defer {
      ZIO
        .fromFuture { _ =>
          context.run(
            DBIO.seq(
              CurrencyTable.schema.createIfNotExists,
              UserTable.schema.createIfNotExists,
              GroupMemberTable.schema.createIfNotExists,
              PaidByTable.schema.createIfNotExists,
              SplitBetweenTable.schema.createIfNotExists,
              ExpenseTable.schema.createIfNotExists,
              GroupTable.schema.createIfNotExists
            )
          )
        }
        .mapError(_.toDomainError())
        .run

      ()
    }
}

class CurrencyEntityTable(tag: Tag) extends Table[CurrencyEntity](tag, None, "CurrencyEntity") {
  val isoCode = column[String]("iso_code", O.PrimaryKey)
  val name = column[String]("name")
  val symbol = column[String]("symbol")

  override def * = (isoCode, name, symbol).mapTo[CurrencyEntity]
}

class UserEntityTable(tag: Tag) extends Table[UserEntity](tag, None, "UserEntity") {
  val uid = column[UserUid]("uid", O.PrimaryKey)
  val name = column[String]("name")

  override def * = (uid, name).mapTo[UserEntity]
}

class GroupMemberEntityTable(tag: Tag) extends Table[GroupMemberEntity](tag, None, "GroupMemberEntity") {
  val uid = column[MemberUid]("uid", O.PrimaryKey)
  val groupUid = column[GroupUid]("group_uid")
  val userUid = column[UserUid]("user_uid")

  override def * = (uid, groupUid, userUid).mapTo[GroupMemberEntity]
}

class PaidByEntityTable(tag: Tag) extends Table[PaidByEntity](tag, None, "PaidByEntity") {
  val groupUid = column[GroupUid]("group_uid")
  val expenseUid = column[ExpenseUid]("expense_uid")
  val memberUid = column[MemberUid]("member_uid")

  override def * = (groupUid, expenseUid, memberUid).mapTo[PaidByEntity]
}

class SplitBetweenEntityTable(tag: Tag) extends Table[SplitBetweenEntity](tag, None, "SplitBetweenEntity") {
  val groupUid = column[GroupUid]("group_uid")
  val expenseUid = column[ExpenseUid]("expense_uid")
  val memberUid = column[MemberUid]("member_uid")

  override def * = (groupUid, expenseUid, memberUid).mapTo[SplitBetweenEntity]
}

class ExpenseEntityTable(tag: Tag) extends Table[ExpenseEntity](tag, None, "ExpenseEntity") {
  val uid = column[ExpenseUid]("uid", O.PrimaryKey)
  val groupUid = column[GroupUid]("group_uid")
  val title = column[String]("title")
  val description = column[String]("description")
  val amount = column[Double]("amount")
  val isSplitBetweenAll = column[Boolean]("is_split_between_all")
  val created = column[Timestamp]("created")
  val modified = column[Timestamp]("modified")

  override def * =
    (uid, groupUid, title, description, amount, isSplitBetweenAll, created, modified).mapTo[ExpenseEntity]
}

class GroupEntityTable(tag: Tag) extends Table[GroupEntity](tag, None, "GroupEntity") {
  val uid = column[GroupUid]("uid", O.PrimaryKey)
  val title = column[String]("title")
  val description = column[String]("description")
  val passwordHash = column[String]("password_hash")
  val currencyIsoCode = column[String]("currency_iso_code")
  val created = column[Timestamp]("created")
  val modified = column[Timestamp]("modified")

  override def * =
    (uid, title, description, passwordHash, currencyIsoCode, created, modified).mapTo[GroupEntity]
}

given userUidColumnType: BaseColumnType[UserUid] = MappedColumnType.base[UserUid, String](
  uid => uid.value.toString,
  value => UserUid(UUID.fromString(value))
)

given groupUidColumnType: BaseColumnType[GroupUid] = MappedColumnType.base[GroupUid, String](
  uid => uid.value.toString,
  value => GroupUid(UUID.fromString(value))
)

given memberUidColumnType: BaseColumnType[MemberUid] = MappedColumnType.base[MemberUid, String](
  uid => uid.value.toString,
  value => MemberUid(UUID.fromString(value))
)

given expenseUidColumnType: BaseColumnType[ExpenseUid] = MappedColumnType.base[ExpenseUid, String](
  uid => uid.value.toString,
  value => ExpenseUid(UUID.fromString(value))
)

given timestampColumnType: BaseColumnType[Timestamp] = MappedColumnType.base[Timestamp, Long](
  timestamp => timestamp.seconds, // Timestamp to Long
  seconds => Timestamp(seconds) // Long to Timestamp
)
