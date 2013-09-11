package com.softwaremill.codebrag.dao

import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.test.mongo.ClearDataAfterTest
import com.typesafe.scalalogging.slf4j.Logging
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.Invitation
import com.foursquare.rogue.LiftRogue._

class MongoInvitationDAOSpec extends FlatSpecWithMongo with ShouldMatchers with ClearDataAfterTest with Logging {

  var invitationDAO: MongoInvitationDAO  = _

  override def beforeEach() {
    super.beforeEach()
    invitationDAO = new MongoInvitationDAO
  }

  private val senderId = new ObjectId()

  private val code = "1234"
  it should "save invitation" taggedAs (RequiresDb) in {
    //given
    val invitation = Invitation(code, senderId)

    //when
    invitationDAO.save(invitation)

    //then
    val invitationRecord = (InvitationRecord where (_.code.eqs(code))).get()
    invitationRecord match {
      case Some(record) => {
        record.code.get should be(code)
        record.invitationSender.get should be(senderId)
      }
      case None => fail()
    }
  }

  it should "load invitation by code" taggedAs (RequiresDb) in {
    //given
    val record = InvitationRecord.createRecord
      .invitationSender(senderId)
      .code(code)
    record.save

    //when
    val invitationOption = invitationDAO.findByCode(code)

    //then
    invitationOption match {
      case Some(invitation) => {
        invitation.code should be(code)
        invitation.invitationSender should be(senderId)
      }
      case None => fail()
    }
  }

  it should "remove invitation by code" taggedAs (RequiresDb) in {
    //given
    val record = InvitationRecord.createRecord
      .invitationSender(senderId)
      .code(code)
    record.save

    //when
    invitationDAO.removeByCode(code)

    //then
    val invitationRecord = (InvitationRecord where (_.code.eqs(code))).get()
    invitationRecord match {
      case Some(_) => fail()
      case None =>
    }
  }
}
