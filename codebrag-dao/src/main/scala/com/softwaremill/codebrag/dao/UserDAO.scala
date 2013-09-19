package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.{CommitInfo, CommitLike, Authentication, User}
import org.bson.types.ObjectId

trait UserDAO {
  /**
   * @return The saved user - in case id was not present, it will be filled after saving.
   */
  def add(user: User): User

  def findAll(): List[User]

  def findById(userId: ObjectId): Option[User]

  def findByEmail(email: String): Option[User]

  def findByLowerCasedLogin(login: String): Option[User]

  def findByLoginOrEmail(loginOrEmail: String): Option[User] = findByLoginOrEmail(loginOrEmail, loginOrEmail)

  def findByLoginOrEmail(login: String, email: String): Option[User]

  def findByToken(token: String): Option[User]

  def changeAuthentication(id: ObjectId, authentication: Authentication)

  def findCommitAuthor(commit: CommitInfo): Option[User]

}
