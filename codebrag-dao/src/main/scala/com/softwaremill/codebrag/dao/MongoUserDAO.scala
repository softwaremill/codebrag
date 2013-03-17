package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.User
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.ObjectIdPk
import com.foursquare.rogue.LiftRogue._

class MongoUserDAO extends UserDAO {

  import UserImplicits._

  override def add(user: User) {
    user.save
  }

  override def findByEmail(email: String) = {
    UserRecord where (_.email eqs email.toLowerCase) get()
  }

  override def findByLowerCasedLogin(login: String) = {
    val userOption: Option[User] = UserRecord where (_.loginLowerCase eqs login.toLowerCase) get()
    userOption match {
      case Some(_) => userOption
      case None => Some(newDummyUser(login))
    }
  }

  override def findByLoginOrEmail(loginOrEmail: String) = {
    val lowercased = loginOrEmail.toLowerCase
    val userOption: Option[User] = UserRecord or(_.where(_.loginLowerCase eqs lowercased), _.where(_.email eqs lowercased)) get()
    userOption match {
      case Some(_) => userOption
      case None => Some(newDummyUser(lowercased))
    }
  }

  def findByToken(token: String) = {
    UserRecord where (_.token eqs token) get()
  }

  private object UserImplicits {
    implicit def fromRecord(user: UserRecord): User = {
      User(user.id.get, user.login.get, user.loginLowerCase.get, user.email.get, user.password.get, user.salt.get, user.token.get)
    }

    implicit def fromRecords(users: List[UserRecord]): List[User] = {
      users.map(fromRecord(_))
    }

    implicit def fromOptionalRecord(userOpt: Option[UserRecord]): Option[User] = {
      userOpt.map(fromRecord(_))
    }

    implicit def toRecord(user: User): UserRecord = {
      UserRecord.createRecord
        .id(user.id)
        .login(user.login)
        .loginLowerCase(user.loginLowerCased)
        .email(user.email)
        .password(user.password)
        .salt(user.salt)
        .token(user.token)
    }
  }

}

private class UserRecord extends MongoRecord[UserRecord] with ObjectIdPk[UserRecord] {
  def meta = UserRecord

  object login extends LongStringField(this)

  object loginLowerCase extends LongStringField(this)

  object email extends LongStringField(this)

  object password extends LongStringField(this)

  object salt extends LongStringField(this)

  object token extends LongStringField(this)

}

private object  UserRecord extends UserRecord with MongoMetaRecord[UserRecord] {
  override def collectionName: String = "users"
}