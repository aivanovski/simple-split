package com.github.ai.split.domain

import com.github.ai.split.data.db.model.PasswordHash
import org.mindrot.jbcrypt.BCrypt

class PasswordService {

  def hashPassword(password: String): PasswordHash =
    PasswordHash(
      value = BCrypt.hashpw(password, BCrypt.gensalt())
    )

  def isPasswordMatch(password: String, hashedPassword: PasswordHash): Boolean = {
    BCrypt.checkpw(password, hashedPassword.value)
  }
}
