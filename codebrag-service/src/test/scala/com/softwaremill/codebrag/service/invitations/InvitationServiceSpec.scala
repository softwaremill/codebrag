package com.softwaremill.codebrag.service.invitations

import org.scalatest.FlatSpec
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.dao.{UserDAO, InvitationDAO}
import com.softwaremill.codebrag.service.email.{Email, EmailService}
import org.mockito.Mockito._
import org.mockito.Matchers._
import com.softwaremill.codebrag.domain.{User, Invitation}
import org.bson.types.ObjectId
import org.mockito.ArgumentCaptor
import com.softwaremill.codebrag.service.config.CodebragConfig
import com.softwaremill.codebrag.service.templates.{EmailContentWithSubject, Templates, EmailTemplateEngine}

class InvitationServiceSpec extends FlatSpec with MockitoSugar with ShouldMatchers {

  private val code = "1234"
  val email = "some@some.some"
  val id = new ObjectId()
  val userName = "zuchos"
  val user = User(id, null, userName, null, null, null)
  val config = mock[CodebragConfig]
  private val appPath = "http://localhost:8080"
  when(config.applicationUrl).thenReturn(appPath)

  it should " verify invitation" in {
    //given
    val invitationDAO = mock[InvitationDAO]
    when(invitationDAO.findByCode(code)).thenReturn(Some(Invitation(code, new ObjectId())))
    val userDAO = mock[UserDAO]
    val emailService = mock[EmailService]
    val emailTemplateEngine = mock[EmailTemplateEngine]

    val invitationService = new InvitationService(invitationDAO, userDAO, emailService, config, DefaultUniqueHashGenerator, emailTemplateEngine)

    //when
    val verify = invitationService.verify(code)

    //then
    verify should be(true)
  }

  it should "should not verify invitation" in {
    //given
    val invitationDAO = mock[InvitationDAO]
    when(invitationDAO.findByCode(code)).thenReturn(None)
    val userDAO = mock[UserDAO]
    val emailService = mock[EmailService]
    val emailTemplateEngine = mock[EmailTemplateEngine]

    val invitationService = new InvitationService(invitationDAO, userDAO, emailService, config, DefaultUniqueHashGenerator, emailTemplateEngine)

    //when
    val verify = invitationService.verify(code)

    //then
    verify should be(false)
  }

  it should "removed expired invitation code from DAO" in {
    //given
    val invitationDAO = mock[InvitationDAO]
    val userDAO = mock[UserDAO]
    val emailService = mock[EmailService]
    val emailTemplateEngine = mock[EmailTemplateEngine]

    val invitationService = new InvitationService(invitationDAO, userDAO, emailService, config, DefaultUniqueHashGenerator, emailTemplateEngine)

    //when
    invitationService.expire(code)

    //then
    verify(invitationDAO).removeByCode(code)
  }

  it should "create invitation message and save invitation in DAO" in {
    //given
    val invitationDAO = mock[InvitationDAO]
    val userDAO = mock[UserDAO]
    val emailService = mock[EmailService]
    val emailTemplateEngine = mock[EmailTemplateEngine]
    val message = "some message"
    when(emailTemplateEngine.getTemplate(any[Templates.Template], any[Map[String, Object]])).thenReturn(EmailContentWithSubject("subject", message))

    val invitationService = new InvitationService(invitationDAO, userDAO, emailService, config, DefaultUniqueHashGenerator, emailTemplateEngine)

    when(userDAO.findById(id)).thenReturn(Some(user))

    //when
    val invitation = invitationService.createInvitation(id)

    //then
    val argumentCaptor = ArgumentCaptor.forClass(classOf[Invitation])
    verify(invitationDAO).save(argumentCaptor.capture())
    argumentCaptor.getValue.invitationSender should be(id)
    invitation should be eq message
  }


  it should "send email with invitation message" in {
    //given

    val invitationDAO = mock[InvitationDAO]
    val userDAO = mock[UserDAO]
    when(userDAO.findById(id)).thenReturn(Some(user))

    val emailService = mock[EmailService]

    val message = "some message"
    val emailTemplateEngine = mock[EmailTemplateEngine]
    when(emailTemplateEngine.getTemplate(any[Templates.Template], any[Map[String, Object]])).thenReturn(EmailContentWithSubject("subject", "some message"))

    val invitationService = new InvitationService(invitationDAO, userDAO, emailService, config, DefaultUniqueHashGenerator, emailTemplateEngine)
    //when
    invitationService.sendInvitation(email, message, id)

    //then
    val argumentCaptor = ArgumentCaptor.forClass(classOf[Email])
    verify(emailService).send(argumentCaptor.capture())

    val emailArgument = argumentCaptor.getValue
    emailArgument.address should be(email)
    emailArgument.content should be(message)
  }


}
