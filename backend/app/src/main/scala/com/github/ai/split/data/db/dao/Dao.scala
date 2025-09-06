package com.github.ai.split.data.db.dao

import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.{toDomainError, some}
import zio.{IO, ZIO}
import slick.jdbc.PostgresProfile.api.*

abstract class Dao[E, TableType <: Table[E]](
  protected val db: Database,
  protected val table: TableQuery[TableType]
) {

  protected def query(
    predicate: TableType => Rep[Boolean]
  ): IO[DomainError, List[E]] = {
    ZIO
      .fromFuture { _ =>
        db.run[Seq[E]](table.filter(predicate).result)
      }
      .map(_.toList)
      .mapError(_.toDomainError())
  }

  protected def queryAll(): IO[DomainError, List[E]] = {
    query(_ => true)
  }

  protected def queryOne(
    predicate: TableType => Rep[Boolean]
  ): IO[DomainError, Option[E]] = {
    query(predicate)
      .map(_.headOption)
  }

  protected def insertAll(entities: List[E]): IO[DomainError, List[E]] = {
    ZIO.collectAll(
      entities.map(entity => insert(entity))
    )
  }

  protected def insert(entity: E): IO[DomainError, E] = {
    ZIO
      .fromFuture { _ => db.run(table += entity) }
      .flatMap { count =>
        if (count != 0) {
          ZIO.succeed(entity)
        } else {
          ZIO.fail(DomainError(message = s"Failed to insert entity: $entity".some))
        }
      }
      .mapError(_.toDomainError())
  }

  protected def updateOne(
    predicate: TableType => Rep[Boolean],
    entity: E
  ): IO[DomainError, E] = {
    ZIO
      .fromFuture { _ =>
        db.run(table.filter(predicate).update(entity))
      }
      .flatMap { count =>
        if (count != 0) {
          ZIO.succeed(entity)
        } else {
          ZIO.fail(DomainError(message = s"Unable to update entity: $entity".some))
        }
      }
      .mapError(_.toDomainError())
  }

  protected def delete(
    predicate: TableType => Rep[Boolean]
  ): IO[DomainError, Unit] = {
    ZIO
      .fromFuture { _ =>
        db.run(table.filter(predicate).delete)
      }
      .map(_ => ())
      .mapError(_.toDomainError())
  }

  protected def deleteOne(
    predicate: TableType => Rep[Boolean]
  ): IO[DomainError, Unit] = {
    ZIO
      .fromFuture { _ =>
        db.run(table.filter(predicate).delete)
      }
      .flatMap { count =>
        if (count != 0) {
          ZIO.succeed(())
        } else {
          ZIO.fail(DomainError(message = s"Unable to delete entity".some))
        }
      }
      .mapError(_.toDomainError())
  }

}
