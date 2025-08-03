package com.github.ai.split.domain

import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.some
import org.mindrot.jbcrypt.BCrypt
import zio.*

class PasswordService {

  def hashPassword(password: String): String =
    BCrypt.hashpw(password, BCrypt.gensalt())

  def isPasswordMatch(password: String, hashedPassword: String): Boolean = {
    BCrypt.checkpw(password, hashedPassword)
  }
}
