package com.softwaremill.codebrag.dao.user

import com.softwaremill.codebrag.domain._
import org.bson.types.ObjectId
import org.joda.time.DateTime

trait UserDAO {
  def add(user: User)

  def findAll(): List[User]

  def findById(userId: ObjectId): Option[User]

  def findByEmail(email: String): Option[User]

  def findByLowerCasedLogin(login: String): Option[User]

  def findByLoginOrEmail(loginOrEmail: String): Option[User] = findByLoginOrEmail(loginOrEmail, loginOrEmail)

  def findByLoginOrEmail(login: String, email: String): Option[User]

  def findByToken(token: String): Option[User]

  def modifyUser(user: User)

  def changeAuthentication(id: ObjectId, authentication: Authentication)

  def rememberNotifications(id: ObjectId, notifications: LastUserNotificationDispatch)

  def changeUserSettings(userID: ObjectId, newSettings: UserSettings)

  def setToReviewStartDate(id: ObjectId, newToReviewDate: DateTime)

  def findCommitAuthor(commit: CommitInfo): Option[User]

  def findPartialUserDetails(names: Iterable[String], emails: Iterable[String]): Iterable[PartialUserDetails]

  def findPartialUserDetails(ids: Iterable[ObjectId]): Iterable[PartialUserDetails]

  def countAll(): Long

  def countAllActive(): Long

  def delete(userId: ObjectId)

}
