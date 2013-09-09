package com.softwaremill.codebrag.service.invitations

import com.softwaremill.codebrag.dao.{UserDAO, InvitationDAO}
import com.softwaremill.codebrag.common.Utils
import com.softwaremill.codebrag.domain.{User, Invitation}
import org.bson.types.ObjectId
import com.softwaremill.codebrag.service.email.{EmailService, Email}

class InvitationService(invitationDAO: InvitationDAO, userDAO: UserDAO, emailService: EmailService) {

  def sendInvitation(emailAddress: String, message: String, invitationSenderId: ObjectId) {
    val option: Option[User] = userDAO.findById(invitationSenderId)
    option match {
      case Some(user) => {
        sendEmail(emailAddress, message, user.name)
      }
      case None => throw new SecurityException("Invitation sender doesn't exist")
    }
  }

  def createInvitation(invitationSenderId: ObjectId, url: String): String = {
    userDAO.findById(invitationSenderId) match {
      case Some(user) => {
        val hash: String = calculateHashForInvitation(new ObjectId().toString)
        saveToDb(hash, invitationSenderId)
        InvitationMessageBuilder.buildMessage(user, url + hash)
      }
      case None => throw new IllegalStateException
    }
  }

  def verify(code: String): Boolean = {
    invitationDAO.findByCode(code) match {
      case Some(inv) => true
      case None => false
    }
  }

  def expire(code: String) {
    invitationDAO.findByCode(code) match {
      case Some(inv) => {
        invitationDAO.removeByCode(code)
      }
      case None => throw new IllegalStateException
    }
  }


  def sendEmail(address: String, message: String, userName: String) {
    emailService.send(Email(address, InvitationMessageBuilder.buildSubjest(userName), message))
  }

  def saveToDb(hash: String, invitationSenderId: ObjectId) {
    invitationDAO.save(Invitation(hash, invitationSenderId))
  }

  private def calculateHashForInvitation(s: String) = {
    Utils.sha1(s + System.currentTimeMillis())
  }

}