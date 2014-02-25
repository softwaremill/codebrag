package com.softwaremill.codebrag.service.invitations

import com.softwaremill.codebrag.common.{Clock, Utils}
import com.softwaremill.codebrag.domain.{User, Invitation}
import org.bson.types.ObjectId
import com.softwaremill.codebrag.service.email.{EmailService, Email}
import com.softwaremill.codebrag.service.config.CodebragConfig
import com.softwaremill.codebrag.service.templates.{EmailTemplates, TemplateEngine}
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.dao.invitation.InvitationDAO
import com.typesafe.scalalogging.slf4j.Logging

class InvitationService(
                         invitationDAO: InvitationDAO,
                         userDAO: UserDAO,
                         emailService: EmailService,
                         config: CodebragConfig,
                         uniqueHashGenerator: UniqueHashGenerator,
                         templateEngine: TemplateEngine)(implicit clock: Clock) extends Logging {

  def sendInvitation(emailAddresses: List[String], registrationLink: String, invitationSenderId: ObjectId) {
    val option: Option[User] = userDAO.findById(invitationSenderId)
    option match {
      case Some(user) => {
        logger.debug(s"Sending invitation ${registrationLink} to ${emailAddresses}")
        sendEmail(emailAddresses, invitationMessage(user.name, registrationLink), user.name)
      }
      case None => throw new SecurityException("Invitation sender doesn't exist")
    }
  }

  def generateInvitationCode(invitationSenderId: ObjectId): String = {
    userDAO.findById(invitationSenderId) match {
      case Some(user) => {
        val invitationCode: String = uniqueHashGenerator.generateUniqueHashCode()
        saveToDb(invitationCode, invitationSenderId)
        invitationCode
      }
      case None => throw new IllegalStateException
    }
  }

  private def getInvitationSubject(userName: String): String = {
    templateEngine.getEmailTemplate(EmailTemplates.Invitation, Map("userName" -> userName)).subject
  }

  private def invitationMessage(senderName: String, invitationLink: String): String = {
    templateEngine.getEmailTemplate(EmailTemplates.Invitation, Map("userName" -> senderName, "url" -> invitationLink)).content
  }

  def verify(code: String): Boolean = {
    invitationDAO.findByCode(code) match {
      case Some(inv) => inv.isValid(clock.nowUtc)
      case None => false
    }
  }

  private def sendEmail(addresses: List[String], message: String, userName: String) {
    emailService.send(Email(addresses, getInvitationSubject(userName), message))
  }

  private def saveToDb(hash: String, invitationSenderId: ObjectId) {
    val expirationTime = clock.nowUtc.plus(config.invitationExpiryTime)
    invitationDAO.save(Invitation(hash, invitationSenderId, expirationTime))
  }

}

trait UniqueHashGenerator {
  def generateUniqueHashCode(): String
}

object DefaultUniqueHashGenerator extends UniqueHashGenerator {
  override def generateUniqueHashCode(): String = {
    //TODO make sure that's the best way to generate unique hash
    Utils.sha1(new ObjectId().toString + System.currentTimeMillis())
  }
}

