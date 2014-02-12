package com.softwaremill.codebrag.dao.user

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.ObjectIdPk
import com.softwaremill.codebrag.domain.InternalUser
import com.foursquare.rogue.LiftRogue._
import com.typesafe.scalalogging.slf4j.Logging
import net.liftweb.record.field.BooleanField
import com.softwaremill.codebrag.dao.mongo.LongStringField

class MongoInternalUserDAO extends InternalUserDAO with Logging {

  def createIfNotExists(internalUser: InternalUser): InternalUser = {
    findUserQuery(internalUser.name) match {
      case Some(user) => {
        logger.debug(s"User ${internalUser.name} already exists. Returning one.")
        fromRecord(user)
      }
      case None => {
        toRecord(internalUser).save
        logger.debug(s"User ${internalUser.name} created.")
        internalUser
      }
    }
  }

  def findByName(internalUserName: String): Option[InternalUser] = {
    findUserQuery(internalUserName).map(fromRecord)
  }

  private def findUserQuery(userName: String) = {
    InternalUserRecord.where(_.name eqs userName).and(_.internal eqs true).get()
  }

  private def toRecord(internalUser: InternalUser) = {
      InternalUserRecord.createRecord
        .internal(true)
        .name(internalUser.name)
        .id(internalUser.id)

  }

  private def fromRecord(internalUserRecord: InternalUserRecord) = {
    InternalUser(internalUserRecord.id.get, internalUserRecord.name.get)
  }

}

class InternalUserRecord extends MongoRecord[InternalUserRecord] with ObjectIdPk[InternalUserRecord] {

  def meta = InternalUserRecord

  object name extends LongStringField(this)
  object internal extends BooleanField(this)

}

object InternalUserRecord extends InternalUserRecord with MongoMetaRecord[InternalUserRecord] {
  override def collectionName = UserRecord.collectionName // use the same collection name as for regular users
}
