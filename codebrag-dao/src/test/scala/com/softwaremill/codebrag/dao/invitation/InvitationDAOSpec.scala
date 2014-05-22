package com.softwaremill.codebrag.dao.invitation

import org.scalatest.matchers.ShouldMatchers
import com.typesafe.scalalogging.slf4j.Logging
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.Invitation
import com.softwaremill.codebrag.test.{ClearSQLDataAfterTest, FlatSpecWithSQL}
import com.softwaremill.codebrag.dao.RequiresDb
import org.scalatest.FlatSpec
import com.softwaremill.codebrag.common.RealTimeClock

trait InvitationDAOSpec extends FlatSpec with ShouldMatchers with Logging {

  def invitationDAO: InvitationDAO

  private val senderId = new ObjectId()

  private val code = "1234"
  it should "save invitation" taggedAs (RequiresDb) in {
    //given
    val invitation = Invitation(code, senderId, RealTimeClock.nowUtc)

    //when
    invitationDAO.save(invitation)

    //then
    val Some(foundInvitation) = invitationDAO.findByCode(code)
    foundInvitation.code should be(code)
    foundInvitation.invitationSender should be(senderId)
    foundInvitation.expiryDate should be(invitation.expiryDate)
  }

  it should "load invitation by code" taggedAs (RequiresDb) in {
    //given
    val invitation = Invitation(code, senderId, RealTimeClock.nowUtc)
    invitationDAO.save(invitation)

    //when
    val Some(invitationOption) = invitationDAO.findByCode(code)

    //then
    invitationOption.code should be(code)
    invitationOption.invitationSender should be(senderId)
  }

  it should "remove invitation by code" taggedAs (RequiresDb) in {
    //given
    val invitation = Invitation(code, senderId, RealTimeClock.nowUtc)
    invitationDAO.save(invitation)

    //when
    invitationDAO.removeByCode(code)

    //then
    val invitationRecord = invitationDAO.findByCode(code)
    invitationRecord should be('empty)
  }
}



class SQLInvitationDAOSpec extends FlatSpecWithSQL with ClearSQLDataAfterTest with InvitationDAOSpec {
  val invitationDAO = new SQLInvitationDAO(sqlDatabase)
}
