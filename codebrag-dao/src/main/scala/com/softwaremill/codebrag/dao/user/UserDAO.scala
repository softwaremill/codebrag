package com.softwaremill.codebrag.dao.user

import com.softwaremill.codebrag.domain._
import org.bson.types.ObjectId

trait UserDAO {
  /**
   * @return The saved user - in case id was not present, it will be filled after saving.
   */
  def add(user: User): User = {
    val toAdd = if (user.id == null) {
      user.copy(id = new ObjectId())
    } else user

    addWithId(toAdd)
  }

  def addWithId(user: User): User

  def findAll(): List[User]

  def findById(userId: ObjectId): Option[User]

  def findByEmail(email: String): Option[User]

  def findByLowerCasedLogin(login: String): Option[User]

  def findByLoginOrEmail(loginOrEmail: String): Option[User] = findByLoginOrEmail(loginOrEmail, loginOrEmail)

  def findByLoginOrEmail(login: String, email: String): Option[User]

  def findByToken(token: String): Option[User]

  def changeAuthentication(id: ObjectId, authentication: Authentication)

  def rememberNotifications(id: ObjectId, notifications: LastUserNotificationDispatch)

  def changeUserSettings(userID: ObjectId, newSettings: UserSettings)

  def findCommitAuthor(commit: CommitInfo): Option[User]

  def findPartialUserDetails(names: Iterable[String], emails: Iterable[String]): Iterable[PartialUserDetails]
}
