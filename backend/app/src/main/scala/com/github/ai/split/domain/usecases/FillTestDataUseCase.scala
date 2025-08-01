package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.dao.{ExpenseEntityDao, GroupEntityDao, GroupMemberEntityDao, PaidByEntityDao, SplitBetweenEntityDao, UserEntityDao}
import com.github.ai.split.domain.PasswordService
import com.github.ai.split.data.db.repository.GroupRepository
import com.github.ai.split.entity.{MemberReference, NameReference, Split, SplitBetweenAll, SplitBetweenMembers}
import com.github.ai.split.entity.db.{ExpenseEntity, ExpenseUid, GroupEntity, GroupMemberEntity, GroupUid, MemberUid, PaidByEntity, SplitBetweenEntity, UserEntity, UserUid}
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.some
import zio.{IO, ZIO}

import java.util.UUID
import java.util.concurrent.atomic.{AtomicInteger, AtomicLong, AtomicReference}

class FillTestDataUseCase(
  private val groupRepository: GroupRepository,
  private val userDao: UserEntityDao,
  private val groupDao: GroupEntityDao,
  private val groupMemberDao: GroupMemberEntityDao,
  private val expenseDao: ExpenseEntityDao,
  private val paidByDao: PaidByEntityDao,
  private val splitBetweenDao: SplitBetweenEntityDao,
  private val passwordService: PasswordService
) {

  import Users._

  private val memberCounter = AtomicLong(1)

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

    ZIO.collectAll(users.map(user => userDao.add(user.toUserEntity()))).map(_ => ())
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
        val members = group.members.map { member =>
          GroupMemberEntity(
            uid = MemberUid(UUID(0L, memberCounter.getAndIncrement() + 1024L)),
            groupUid = group.uid,
            userUid = member.userUid
          )
        }

        groupMemberDao.add(members)
      }

      _ <- ZIO.collectAll(
        group.expenses.map {
          expense =>
            insertExpense(
              groupUid = group.uid,
              expense = expense
            )
        }
      )
    } yield ()
  }

  private def insertExpense(
    groupUid: GroupUid,
    expense: Expense
  ): IO[DomainError, Unit] = {
    for {
      members <- groupRepository.getMembers(groupUid)

      userUidToMemberUidMap = members.map(member => (member.user.uid, member.entity.uid)).toMap
      userNameToMemberUidMap = members.map(member => (member.user.name, member.entity.uid)).toMap

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

      paidBy <- ZIO.collectAll(
        expense.paidBy.map { payerUserUid =>
          val memberUid = userUidToMemberUidMap.get(payerUserUid)
          if (memberUid.isDefined) {
            ZIO.succeed(
              PaidByEntity(
                groupUid = groupUid,
                expenseUid = expense.uid,
                memberUid = memberUid.get
              )
            )
          } else {
            ZIO.fail(DomainError())
          }
        }
      )

      _ <- paidByDao.add(paidBy)

      _ <- {
        expense.split match {
          case SplitBetweenMembers(references) => {
            ZIO.collectAll(
                references
                  .map { reference =>
                    val name = reference.asInstanceOf[NameReference].name
                    val memberUid = userNameToMemberUidMap.get(name)

                    if (memberUid.isDefined) {
                      ZIO.succeed(memberUid.get)
                    } else {
                      ZIO.fail(DomainError(message = s"Failed to resolve member by name: $name".some))
                    }
                  }

              ).map { memberUids =>
                memberUids.map { memberUid =>
                  SplitBetweenEntity(
                    groupUid = groupUid,
                    expenseUid = expense.uid,
                    memberUid = memberUid
                  )
                }
              }
              .flatMap { splits =>
                splitBetweenDao.add(splits)
              }
          }

          case SplitBetweenAll => {
            ZIO.succeed(())
          }
        }
      }
    } yield ()
  }

  private def createTripToDisneyLandGroup(): Group = {
    Group(
      uid = GroupUid(UUID.fromString("00000000-0000-0000-0000-b00000000001")),
      title = "Trip to Disney Land",
      description = "Just a regular trip on weekend",
      password = "abc123",
      members = List(Mickey, Donald, Goofy),
      expenses = List(
        Expense(
          uid = ExpenseUid(UUID.fromString("00000000-0000-0000-0000-e00000000001")),
          title = "Train tickets",
          description = "",
          amount = 300.0,
          paidBy = List(Mickey.userUid),
          split = SplitBetweenAll
        )
      )
    )
  }

  private def createCoffeeShopRegularsGroup(): Group = {
    Group(
      uid = GroupUid(UUID.fromString("00000000-0000-0000-0000-b00000000002")),
      title = "Coffee Shop Regulars",
      description = "Daily coffee expenses tracking",
      password = "abc123",
      members = List(Mickey, Minnie),
      expenses = List(
        Expense(
          uid = ExpenseUid(UUID.fromString("00000000-0000-0000-0000-e00000000002")),
          title = "Morning Coffee",
          description = "Cappuccino and Latte",
          amount = 15.50,
          paidBy = List(Mickey.userUid),
          split = SplitBetweenAll
        ),
        Expense(
          uid = ExpenseUid(UUID.fromString("00000000-0000-0000-0000-e00000000003")),
          title = "Pastries",
          description = "Croissants and muffins",
          amount = 22.00,
          paidBy = List(Minnie.userUid),
          split = SplitBetweenMembers(
            members = List(NameReference(Minnie.name))
          )
        )
      )
    )
  }

  private def createFamilyDinnerGroup(): Group = {
    Group(
      uid = GroupUid(UUID.fromString("00000000-0000-0000-0000-b00000000003")),
      title = "Family Dinner",
      description = "Monthly family gathering expenses",
      password = "abc123",
      members = List(Donald, Chip, Dale, Daisy),
      expenses = List(
        Expense(
          uid = ExpenseUid(UUID.fromString("00000000-0000-0000-0000-e00000000004")),
          title = "Restaurant Bill",
          description = "Italian restaurant dinner",
          amount = 180.00,
          paidBy = List(Donald.userUid),
          split = SplitBetweenAll
        ),
        Expense(
          uid = ExpenseUid(UUID.fromString("00000000-0000-0000-0000-e00000000005")),
          title = "Groceries",
          description = "Ingredients for homemade dessert",
          amount = 45.75,
          paidBy = List(Chip.userUid),
          split = SplitBetweenMembers(
            members = List(Chip, Dale).map(m => NameReference(m.name))
          )
        )
      )
    )
  }

  private def createBookClubGroup(): Group = {
    Group(
      uid = GroupUid(UUID.fromString("00000000-0000-0000-0000-b00000000004")),
      title = "Book Club",
      description = "Monthly book purchases and meeting expenses",
      password = "abc123",
      members = List(Goofy, Minnie, Pluto, Scrooge, Huey),
      expenses = List(
        Expense(
          uid = ExpenseUid(UUID.fromString("00000000-0000-0000-0000-e00000000006")),
          title = "Monthly Books",
          description = "Book purchases for this month",
          amount = 85.00,
          paidBy = List(Scrooge.userUid),
          split = SplitBetweenAll
        ),
        Expense(
          uid = ExpenseUid(UUID.fromString("00000000-0000-0000-0000-e00000000007")),
          title = "Meeting Snacks",
          description = "Coffee and cookies for book discussion",
          amount = 32.50,
          paidBy = List(Minnie.userUid),
          split = SplitBetweenMembers(
            members = List(Minnie, Pluto, Huey).map(m => NameReference(m.name))
          )
        )
      )
    )
  }

  private def createSportsTeamGroup(): Group = {
    Group(
      uid = GroupUid(UUID.fromString("00000000-0000-0000-0000-b00000000005")),
      title = "Sports Team",
      description = "Basketball team equipment and tournament fees",
      password = "abc123",
      members = List(Mickey, Goofy, Chip, Dale, Daisy, Huey),
      expenses = List(
        Expense(
          uid = ExpenseUid(UUID.fromString("00000000-0000-0000-0000-e00000000008")),
          title = "Tournament Entry Fee",
          description = "Registration for regional tournament",
          amount = 240.00,
          paidBy = List(Mickey.userUid),
          split = SplitBetweenAll
        ),
        Expense(
          uid = ExpenseUid(UUID.fromString("00000000-0000-0000-0000-e00000000009")),
          title = "Team Jerseys",
          description = "Custom basketball jerseys",
          amount = 180.00,
          paidBy = List(Goofy.userUid),
          split = SplitBetweenMembers(
            members = List(Goofy, Chip, Dale).map(m => NameReference(m.name))
          )
        ),
        Expense(
          uid = ExpenseUid(UUID.fromString("00000000-0000-0000-0000-e00000000010")),
          title = "Post-Game Pizza",
          description = "Celebration dinner after winning",
          amount = 95.00,
          paidBy = List(Daisy.userUid),
          split = SplitBetweenAll
        )
      )
    )
  }

  private object Users {
    val Mickey = User(UserUid(UUID.fromString("00000000-0000-0000-0000-a00000000001")), "Mickey Mouse")
    val Donald = User(UserUid(UUID.fromString("00000000-0000-0000-0000-a00000000002")), "Donald Duck")
    val Goofy = User(UserUid(UUID.fromString("00000000-0000-0000-0000-a00000000003")), "Goofy")
    val Chip = User(UserUid(UUID.fromString("00000000-0000-0000-0000-a00000000004")), "Chip")
    val Dale = User(UserUid(UUID.fromString("00000000-0000-0000-0000-a00000000005")), "Dale")
    val Minnie = User(UserUid(UUID.fromString("00000000-0000-0000-0000-a00000000006")), "Minnie Mouse")
    val Pluto = User(UserUid(UUID.fromString("00000000-0000-0000-0000-a00000000007")), "Pluto")
    val Daisy = User(UserUid(UUID.fromString("00000000-0000-0000-0000-a00000000008")), "Daisy Duck")
    val Scrooge = User(UserUid(UUID.fromString("00000000-0000-0000-0000-a00000000009")), "Scrooge McDuck")
    val Huey = User(UserUid(UUID.fromString("00000000-0000-0000-0000-a00000000010")), "Huey Duck")
  }

  extension (user: User) {
    private def toUserEntity(): UserEntity = UserEntity(user.userUid, user.name)
  }

  private case class User(
    userUid: UserUid,
    name: String
  )

  private case class Group(
    uid: GroupUid,
    title: String,
    description: String,
    password: String,
    members: List[User],
    expenses: List[Expense]
  )

  private case class Expense(
    uid: ExpenseUid,
    title: String,
    description: String,
    amount: Double,
    paidBy: List[UserUid],
    split: Split
  )
}