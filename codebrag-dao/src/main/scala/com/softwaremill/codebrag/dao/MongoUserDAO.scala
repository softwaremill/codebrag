package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.User
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.ObjectIdPk
import com.foursquare.rogue.LiftRogue._
import org.bson.types.ObjectId

class MongoUserDAO extends UserDAO {

  import UserImplicits._

  def loadAll = {
    UserRecord.findAll
  }

  def countItems(): Long = {
    UserRecord.count
  }

  protected def internalAddUser(user: User) {
    user.save
  }

  def remove(userId: String) {
    UserRecord where (_.id eqs new ObjectId(userId)) findAndDeleteOne()
  }

  override def findForIdentifiers(ids: List[ObjectId]): List[User] =
    UserRecord findAllByList(ids)

  def load(userId: String): Option[User] = {
    UserRecord where (_.id eqs new ObjectId(userId)) get()
  }

  def findByEmail(email: String) = {
    UserRecord where (_.email eqs email.toLowerCase) get()
  }

  def findByLowerCasedLogin(login: String) = {
    UserRecord where (_.loginLowerCase eqs login.toLowerCase) get()
  }

  def findByLoginOrEmail(loginOrEmail: String) = {
    val lowercased = loginOrEmail.toLowerCase
    UserRecord or(_.where(_.loginLowerCase eqs lowercased), _.where(_.email eqs lowercased)) get()
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