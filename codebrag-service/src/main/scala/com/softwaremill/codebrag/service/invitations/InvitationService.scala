package com.softwaremill.codebrag.service.invitations

import com.softwaremill.codebrag.dao.{UserDAO, InvitationDAO}
import com.softwaremill.codebrag.common.{Clock, Utils}
import com.softwaremill.codebrag.domain.{User, Invitation}
import org.bson.types.ObjectId
import com.softwaremill.codebrag.service.email.{EmailService, Email}
import com.softwaremill.codebrag.service.config.CodebragConfig
import com.softwaremill.codebrag.service.templates.{Templates, EmailTemplateEngine}
import org.joda.time.{Minutes, Hours}

class InvitationService(
                         invitationDAO: InvitationDAO,
                         userDAO: UserDAO,
                         emailService: EmailService,
                         config: CodebragConfig,
                         uniqueHashGenerator: UniqueHashGenerator,
                         templateEngine: EmailTemplateEngine)(implicit clock: Clock) {

  val registrationUrl = buildRegistrationUrl()

  def sendInvitation(emailAddress: String, message: String, invitationSenderId: ObjectId) {
    val option: Option[User] = userDAO.findById(invitationSenderId)
    option match {
      case Some(user) => {
        sendEmail(emailAddress, message, user.name)
      }
      case None => throw new SecurityException("Invitation sender doesn't exist")
    }
  }

  def createInvitation(invitationSenderId: ObjectId): String = {
    userDAO.findById(invitationSenderId) match {
      case Some(user) => {
        val invitationCode: String = uniqueHashGenerator.generateUniqueHashCode()
        saveToDb(invitationCode, invitationSenderId)
        getInvitationMessage(user, invitationCode)
      }
      case None => throw new IllegalStateException
    }
  }


  private def getInvitationMessage(user: User, invitationCode: String): String = {
    templateEngine.getTemplate(Templates.Invitation, Map("userName" -> user.name, "url" -> registrationUrl.urlForCode(invitationCode))).content
  }

  private def getInvitationSubject(userName: String): String = {
    templateEngine.getTemplate(Templates.Invitation, Map("userName" -> userName)).subject
  }

  def verify(code: String): Boolean = {
    invitationDAO.findByCode(code) match {
      case Some(inv) => inv.isValid(clock.currentDateTimeUTC)
      case None => false
    }
  }

  def expire(code: String) {
    invitationDAO.removeByCode(code)
  }


  private def sendEmail(address: String, message: String, userName: String) {
    emailService.send(Email(address, getInvitationSubject(userName), message))
  }

  private def saveToDb(hash: String, invitationSenderId: ObjectId) {
    val expirationTime = clock.currentDateTimeUTC.plus(InvitationService.INVITATION_CODE_EXPIRATION_TIME)
    invitationDAO.save(Invitation(hash, invitationSenderId, expirationTime))
  }

  private def buildRegistrationUrl(): RegistrationUrl = {
    val url = new StringBuilder(config.applicationUrl)
    if (!url.endsWith("/")) {
      url.append("/")
    }
    url.append("#/register/{invitationCode}")
    new RegistrationUrl(url.toString())
  }

  class RegistrationUrl(url: String) {
    def urlForCode(invitationCode: String): String = {
      url.replace("{invitationCode}", invitationCode)
    }
  }

}

object InvitationService {
  val INVITATION_CODE_EXPIRATION_TIME = Minutes.minutes(1)
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

