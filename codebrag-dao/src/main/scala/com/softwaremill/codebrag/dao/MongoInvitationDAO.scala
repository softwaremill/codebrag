package com.softwaremill.codebrag.dao

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.domain.Invitation
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.{DateField, ObjectIdField, ObjectIdPk}
import com.foursquare.rogue.LiftRogue._
import org.joda.time.DateTime

class MongoInvitationDAO extends InvitationDAO with Logging {

  override def save(invitation: Invitation) {
    InvitationToRecordBuilder.buildFrom(invitation).save
  }

  override def findByCode(code: String): Option[Invitation] = {
    (InvitationRecord where (_.code.eqs(code))).get().map(RecordToInvitationBuilder.buildFrom(_))
  }

  override def removeByCode(code: String) {
    val invitationOpt = (InvitationRecord where (_.code.eqs(code))).get()
    invitationOpt match {
      case Some(invitationRecord) => invitationRecord.delete_!
      case None => logger.warn(s"No Invitation with code $code. Cannot delete it.")
    }

  }
}

private object InvitationToRecordBuilder {

  def buildFrom(invitation: Invitation) = {
    InvitationRecord.createRecord
      .invitationSender(invitation.invitationSender)
      .expiryDate(invitation.expiryDate.toDate)
      .code(invitation.code)
  }

}

private object RecordToInvitationBuilder {

  def buildFrom(record: InvitationRecord) = {
    Invitation(record.code.get, record.invitationSender.get, new DateTime(record.expiryDate.get))
  }
}

class InvitationRecord extends MongoRecord[InvitationRecord] with ObjectIdPk[InvitationRecord] {

  def meta = InvitationRecord

  object code extends LongStringField(this)

  object invitationSender extends ObjectIdField(this)

  object expiryDate extends DateField(this)

}

object InvitationRecord extends InvitationRecord with MongoMetaRecord[InvitationRecord] {
  override def collectionName = "invitations"
}

