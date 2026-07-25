package com.github.ai.split.domain

import org.mindrot.jbcrypt.BCrypt

class PasswordService {

  def hashPassword(password: String): String =
    BCrypt.hashpw(password, BCrypt.gensalt())

  def isPasswordMatch(password: String, hashedPassword: String): Boolean = {
    BCrypt.checkpw(password, hashedPassword)
  }
}
