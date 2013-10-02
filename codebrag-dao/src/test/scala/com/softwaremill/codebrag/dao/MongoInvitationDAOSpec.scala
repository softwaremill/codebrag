package com.softwaremill.codebrag.dao

import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.test.mongo.ClearDataAfterTest
import com.typesafe.scalalogging.slf4j.Logging
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.Invitation
import com.foursquare.rogue.LiftRogue._
import org.joda.time.DateTime

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
    val invitation = Invitation(code, senderId, DateTime.now)

    //when
    invitationDAO.save(invitation)

    //then
    val Some(invitationRecord) = (InvitationRecord where (_.code.eqs(code))).get()
    invitationRecord.code.get should be(code)
    invitationRecord.invitationSender.get should be(senderId)
    invitationRecord.expiryDate.get should be(invitation.expiryDate.toDate)
  }

  it should "load invitation by code" taggedAs (RequiresDb) in {
    //given
    val record = InvitationRecord.createRecord
      .invitationSender(senderId)
      .code(code)
    record.save

    //when
    val Some(invitationOption) = invitationDAO.findByCode(code)

    //then
    invitationOption.code should be(code)
    invitationOption.invitationSender should be(senderId)
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
    invitationRecord should be('empty)
  }
}
