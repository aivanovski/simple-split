package com.github.ai.split.domain

import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.some
import org.mindrot.jbcrypt.BCrypt
import zio.*

class PasswordService {

  def hashPassword(password: String): String =
    BCrypt.hashpw(password, BCrypt.gensalt())

  def verifyPassword(password: String, hashedPassword: String): IO[DomainError, Unit] = {
    val isMatch = BCrypt.checkpw(password, hashedPassword)
    if (isMatch) {
      ZIO.succeed(())
    } else {
      ZIO.fail(DomainError(message = "Password doesn't match".some))
    }
  }
}
