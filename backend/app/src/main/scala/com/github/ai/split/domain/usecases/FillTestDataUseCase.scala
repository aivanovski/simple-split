package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.dao.{ExpenseEntityDao, GroupEntityDao, GroupMemberEntityDao, PaidByEntityDao, SplitBetweenEntityDao, UserEntityDao}
import com.github.ai.split.domain.PasswordService
import com.github.ai.split.entity.{Split, SplitBetweenAll, SplitBetweenMembers}
import com.github.ai.split.entity.db.{ExpenseEntity, GroupEntity, GroupMemberEntity, PaidByEntity, SplitBetweenEntity, UserEntity}
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.some
import zio.{IO, ZIO}

import java.util.UUID

class FillTestDataUseCase(
  private val userDao: UserEntityDao,
  private val groupDao: GroupEntityDao,
  private val groupMemberDao: GroupMemberEntityDao,
  private val expenseDao: ExpenseEntityDao,
  private val paidByDao: PaidByEntityDao,
  private val splitBetweenDao: SplitBetweenEntityDao,
  private val passwordService: PasswordService
) {

  import Users._

  def createTestData(): IO[DomainError, Unit] = {
    for {
      _ <- ZIO.logInfo("Creating test data in database...")

      // Check if data already exists
      // TODO: rewrite check
      existingUsers <- userDao.getAll().catchAll(_ => ZIO.succeed(List.empty))

      _ <- if (existingUsers.nonEmpty) {
        ZIO.logInfo("Database already contains test data")
      } else {
        val groups = List(
          createTripToDisneyLandGroup(),
          createCoffeeShopRegularsGroup(),
          createFamilyDinnerGroup(),
          createBookClubGroup(),
          createSportsTeamGroup()
        )

        for {
          _ <- insertUsers()
          _ <- insertGroups(groups)
          _ <- ZIO.logInfo("Test data inserted successfully")
        } yield ()
      }
    } yield ()
  }

  private def insertUsers(): IO[DomainError, Unit] = {
    val users = List(
      Mickey,
      Donald,
      Goofy,
      Chip,
      Dale,
      Minnie,
      Pluto,
      Daisy,
      Scrooge,
      Huey
    )

    ZIO.collectAll(users.map(userDao.add)).map(_ => ())
  }

  private def insertGroups(groups: List[Group]): IO[DomainError, Unit] = {
    for {
      _ <- ZIO.collectAll(groups.map(group => insertGroup(group)))
    } yield ()
  }

  private def insertGroup(group: Group): IO[DomainError, Unit] = {
    for {
      _ <- groupDao.add(
        GroupEntity(
          uid = group.uid,
          title = group.title,
          description = group.description,
          passwordHash = Some(passwordService.hashPassword(group.password))
        )
      )

      _ <- {
        val members = group.members.map { memberUid =>
          GroupMemberEntity(groupUid = group.uid, userUid = memberUid)
        }

        groupMemberDao.add(members)
      }

      _ <- ZIO.collectAll(
        group.expenses.map(expense => insertExpense(groupUid = group.uid, expense = expense))
      )
    } yield ()
  }

  private def insertExpense(
    groupUid: UUID,
    expense: Expense
  ): IO[DomainError, Unit] = {
    for {
      _ <- expenseDao.add(
        ExpenseEntity(
          uid = expense.uid,
          groupUid = groupUid,
          title = expense.title,
          description = expense.description,
          amount = expense.amount,
          isSplitBetweenAll = expense.split match {
            case SplitBetweenAll => true
            case SplitBetweenMembers(_) => false
          }
        )
      )

      _ <- {
        val paidBy = expense.paidBy.map { payerUid =>
          PaidByEntity(
            groupUid = groupUid,
            expenseUid = expense.uid,
            userUid = payerUid
          )
        }

        paidByDao.add(paidBy)
      }

      _ <- {
        val splitUserUids = expense.split match {
          case SplitBetweenMembers(userUids) => userUids
          case SplitBetweenAll => List.empty
        }

        if (splitUserUids.nonEmpty) {
          val splits = splitUserUids.map { uid =>
            SplitBetweenEntity(
              groupUid = groupUid,
              expenseUid = expense.uid,
              userUid = uid
            )
          }

          splitBetweenDao.add(splits)
        } else {
          ZIO.succeed(())
        }
      }
    } yield ()
  }

  private def createTripToDisneyLandGroup(): Group = {
    Group(
      uid = UUID.fromString("00000000-0000-0000-0000-b00000000001"),
      title = "Trip to Disney Land",
      description = "Just a regular trip on weekend",
      password = "abc123",
      members = List(Mickey.uid, Donald.uid, Goofy.uid),
      expenses = List(
        Expense(
          uid = UUID.fromString("00000000-0000-0000-0000-e00000000001"),
          title = "Train tickets",
          description = "",
          amount = 300.0,
          paidBy = List(Mickey.uid),
          split = SplitBetweenAll
        )
      )
    )
  }

  private def createCoffeeShopRegularsGroup(): Group = {
    Group(
      uid = UUID.fromString("00000000-0000-0000-0000-b00000000002"),
      title = "Coffee Shop Regulars",
      description = "Daily coffee expenses tracking",
      password = "abc123",
      members = List(Mickey.uid, Minnie.uid),
      expenses = List(
        Expense(
          uid = UUID.fromString("00000000-0000-0000-0000-e00000000002"),
          title = "Morning Coffee",
          description = "Cappuccino and Latte",
          amount = 15.50,
          paidBy = List(Mickey.uid),
          split = SplitBetweenAll
        ),
        Expense(
          uid = UUID.fromString("00000000-0000-0000-0000-e00000000003"),
          title = "Pastries",
          description = "Croissants and muffins",
          amount = 22.00,
          paidBy = List(Minnie.uid),
          split = SplitBetweenMembers(List(Minnie.uid))
        )
      )
    )
  }

  private def createFamilyDinnerGroup(): Group = {
    Group(
      uid = UUID.fromString("00000000-0000-0000-0000-b00000000003"),
      title = "Family Dinner",
      description = "Monthly family gathering expenses",
      password = "abc123",
      members = List(Donald.uid, Chip.uid, Dale.uid, Daisy.uid),
      expenses = List(
        Expense(
          uid = UUID.fromString("00000000-0000-0000-0000-e00000000004"),
          title = "Restaurant Bill",
          description = "Italian restaurant dinner",
          amount = 180.00,
          paidBy = List(Donald.uid),
          split = SplitBetweenAll
        ),
        Expense(
          uid = UUID.fromString("00000000-0000-0000-0000-e00000000005"),
          title = "Groceries",
          description = "Ingredients for homemade dessert",
          amount = 45.75,
          paidBy = List(Chip.uid),
          split = SplitBetweenMembers(List(Chip.uid, Dale.uid))
        )
      )
    )
  }

  private def createBookClubGroup(): Group = {
    Group(
      uid = UUID.fromString("00000000-0000-0000-0000-b00000000004"),
      title = "Book Club",
      description = "Monthly book purchases and meeting expenses",
      password = "abc123",
      members = List(Goofy.uid, Minnie.uid, Pluto.uid, Scrooge.uid, Huey.uid),
      expenses = List(
        Expense(
          uid = UUID.fromString("00000000-0000-0000-0000-e00000000006"),
          title = "Monthly Books",
          description = "Book purchases for this month",
          amount = 85.00,
          paidBy = List(Scrooge.uid),
          split = SplitBetweenAll
        ),
        Expense(
          uid = UUID.fromString("00000000-0000-0000-0000-e00000000007"),
          title = "Meeting Snacks",
          description = "Coffee and cookies for book discussion",
          amount = 32.50,
          paidBy = List(Minnie.uid),
          split = SplitBetweenMembers(List(Minnie.uid, Pluto.uid, Huey.uid))
        )
      )
    )
  }

  private def createSportsTeamGroup(): Group = {
    Group(
      uid = UUID.fromString("00000000-0000-0000-0000-b00000000005"),
      title = "Sports Team",
      description = "Basketball team equipment and tournament fees",
      password = "abc123",
      members = List(Mickey.uid, Goofy.uid, Chip.uid, Dale.uid, Daisy.uid, Huey.uid),
      expenses = List(
        Expense(
          uid = UUID.fromString("00000000-0000-0000-0000-e00000000008"),
          title = "Tournament Entry Fee",
          description = "Registration for regional tournament",
          amount = 240.00,
          paidBy = List(Mickey.uid),
          split = SplitBetweenAll
        ),
        Expense(
          uid = UUID.fromString("00000000-0000-0000-0000-e00000000009"),
          title = "Team Jerseys",
          description = "Custom basketball jerseys",
          amount = 180.00,
          paidBy = List(Goofy.uid),
          split = SplitBetweenMembers(List(Goofy.uid, Chip.uid, Dale.uid))
        ),
        Expense(
          uid = UUID.fromString("00000000-0000-0000-0000-e00000000010"),
          title = "Post-Game Pizza",
          description = "Celebration dinner after winning",
          amount = 95.00,
          paidBy = List(Daisy.uid),
          split = SplitBetweenAll
        )
      )
    )
  }

  object Users {
    val Mickey: UserEntity = UserEntity(UUID.fromString("00000000-0000-0000-0000-a00000000001"), "Mickey Mouse")
    val Donald: UserEntity = UserEntity(UUID.fromString("00000000-0000-0000-0000-a00000000002"), "Donald Duck")
    val Goofy: UserEntity = UserEntity(UUID.fromString("00000000-0000-0000-0000-a00000000003"), "Goofy")
    val Chip: UserEntity = UserEntity(UUID.fromString("00000000-0000-0000-0000-a00000000004"), "Chip")
    val Dale: UserEntity = UserEntity(UUID.fromString("00000000-0000-0000-0000-a00000000005"), "Dale")
    val Minnie: UserEntity = UserEntity(UUID.fromString("00000000-0000-0000-0000-a00000000006"), "Minnie Mouse")
    val Pluto: UserEntity = UserEntity(UUID.fromString("00000000-0000-0000-0000-a00000000007"), "Pluto")
    val Daisy: UserEntity = UserEntity(UUID.fromString("00000000-0000-0000-0000-a00000000008"), "Daisy Duck")
    val Scrooge: UserEntity = UserEntity(UUID.fromString("00000000-0000-0000-0000-a00000000009"), "Scrooge McDuck")
    val Huey: UserEntity = UserEntity(UUID.fromString("00000000-0000-0000-0000-a00000000010"), "Huey Duck")
  }

  private case class Group(
    uid: UUID,
    title: String,
    description: String,
    password: String,
    members: List[UUID],
    expenses: List[Expense]
  )

  private case class Expense(
    uid: UUID,
    title: String,
    description: String,
    amount: Double,
    paidBy: List[UUID],
    split: Split
  )
}