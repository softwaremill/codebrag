package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.{Authentication, User}
import net.liftweb.mongodb.record.{BsonMetaRecord, BsonRecord, MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.{BsonRecordField, ObjectIdPk}
import com.foursquare.rogue.LiftRogue._
import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.EventBus

class MongoUserDAO(val eventBus: EventBus) extends UserDAO {

  import UserImplicits._

  override def add(user: User) {
    user.save
  }

  override def findAll() = {
    UserRecord.findAll
  }

  override def findByEmail(email: String) = {
    UserRecord where (_.email eqs email.toLowerCase) get()
  }

  override def findByLowerCasedLogin(login: String) = {
    val userOption: Option[User] = UserRecord where (_.authentication.subfield(_.usernameLowerCase) eqs login.toLowerCase) get()
    userOption match {
      case Some(_) => userOption
      case None => Some(createAndSaveDummyUser(login))
    }
  }

  override def findByLoginOrEmail(loginOrEmail: String) = {
    val lowercased = loginOrEmail.toLowerCase
    val userOption: Option[User] = UserRecord or(_.where(_.authentication.subfield(_.usernameLowerCase) eqs lowercased), _.where(_.email eqs lowercased)) get()
    userOption match {
      case Some(_) => userOption
      case None => Some(createAndSaveDummyUser(lowercased))
    }
  }

  def findByUserName(userName: String) = {
    UserRecord where (_.name eqs userName) get()
  }

  def findByToken(token: String) = {
    UserRecord where (_.token eqs token) get()
  }

  def findById(userId: ObjectId) = {
    UserRecord.where(_.id eqs userId).get()
  }

  def changeAuthentication(id: ObjectId, authentication: Authentication) {
    UserRecord where (_.id eqs id) modify (_.authentication setTo (authentication)) updateOne()
  }

  private object UserImplicits {
    implicit def fromRecord(user: UserRecord): User = {
      User(user.id.get, user.authentication.get, user.name.get, user.email.get, user.token.get, user.avatarUrl.get)
    }

    implicit def fromRecords(users: List[UserRecord]): List[User] = {
      users.map(fromRecord(_))
    }

    implicit def fromOptionalRecord(userOpt: Option[UserRecord]): Option[User] = {
      userOpt.map(fromRecord(_))
    }

    implicit def toRecord(user: User): UserRecord = {
      UserRecord.createRecord.id(user.id)
        .name(user.name)
        .token(user.token)
        .email(user.email)
        .authentication(user.authentication)
        .avatarUrl(user.avatarUrl)
    }

    implicit def toRecord(authentication: Authentication): AuthenticationRecord = {
      AuthenticationRecord.createRecord
        .provider(authentication.provider)
        .username(authentication.username)
        .usernameLowerCase(authentication.usernameLowerCase)
        .token(authentication.token)
        .salt(authentication.salt)
    }

    implicit def fromRecord(record: AuthenticationRecord): Authentication = {
      Authentication(record.provider.get, record.username.get, record.usernameLowerCase.get, record.token.get, record.salt.get)
    }
  }

}

private class UserRecord extends MongoRecord[UserRecord] with ObjectIdPk[UserRecord] {
  def meta = UserRecord

  object authentication extends BsonRecordField(this, AuthenticationRecord)

  object name extends LongStringField(this)

  object email extends LongStringField(this)

  object token extends LongStringField(this)

  object avatarUrl extends LongStringField(this)

}

private object UserRecord extends UserRecord with MongoMetaRecord[UserRecord] {
  override def collectionName = "users"
}

private class AuthenticationRecord extends BsonRecord[AuthenticationRecord] {
  def meta = AuthenticationRecord

  object provider extends LongStringField(this)

  object username extends LongStringField(this)

  object usernameLowerCase extends LongStringField(this)

  object token extends LongStringField(this)

  object salt extends LongStringField(this)
}

private object AuthenticationRecord extends AuthenticationRecord with BsonMetaRecord[AuthenticationRecord]