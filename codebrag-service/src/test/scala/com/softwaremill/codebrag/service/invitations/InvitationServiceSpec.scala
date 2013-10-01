package com.softwaremill.codebrag.service.invitations

import org.scalatest.{BeforeAndAfterEach, FlatSpec}
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
import com.softwaremill.codebrag.common.FixtureTimeClock

class InvitationServiceSpec extends FlatSpec with MockitoSugar with ShouldMatchers with BeforeAndAfterEach {

  private val code = "1234"
  val email = "some@some.some"
  val id = new ObjectId()
  val userName = "zuchos"
  val user = User(id, null, userName, null, null, null)
  val config = mock[CodebragConfig]

  val clock = new FixtureTimeClock(System.currentTimeMillis())
  var invitationDAO: InvitationDAO = _
  var userDAO: UserDAO = _
  var emailService: EmailService = _
  var emailTemplateEngine: EmailTemplateEngine = _

  var invitationService: InvitationService = _

  private val appPath = "http://localhost:8080"
  when(config.applicationUrl).thenReturn(appPath)

  override def beforeEach() {
    invitationDAO = mock[InvitationDAO]
    userDAO = mock[UserDAO]
    emailService = mock[EmailService]
    emailTemplateEngine = mock[EmailTemplateEngine]
    invitationService = new InvitationService(invitationDAO, userDAO, emailService, config, DefaultUniqueHashGenerator, emailTemplateEngine)(clock)
  }

  it should "positively verify invitation" in {
    //given
    val expirationInFuture = clock.currentDateTimeUTC.plusHours(1)
    when(invitationDAO.findByCode(code)).thenReturn(Some(Invitation(code, new ObjectId(), expirationInFuture)))

    //when
    val verify = invitationService.verify(code)

    //then
    verify should be(true)
  }

  it should "negatively verify invitation when invitation expired" in {
    //given
    val expirationTimeInThePast = clock.currentDateTimeUTC.minusHours(1)
    when(invitationDAO.findByCode(code)).thenReturn(Some(Invitation(code, new ObjectId(), expirationTimeInThePast)))

    //when
    val verify = invitationService.verify(code)

    //then
    verify should be(false)
  }

  it should "negatively verify invitation when code not found" in {
    //given
    when(invitationDAO.findByCode(code)).thenReturn(None)

    //when
    val verify = invitationService.verify(code)

    //then
    verify should be(false)
  }

  it should "removed expired invitation code from DAO" in {
    //given

    //when
    invitationService.expire(code)

    //then
    verify(invitationDAO).removeByCode(code)
  }

  it should "create invitation message and save invitation in DAO" in {
    //given
    val message = "some message"
    when(emailTemplateEngine.getTemplate(any[Templates.Template], any[Map[String, Object]])).thenReturn(EmailContentWithSubject(message, "subject"))
    when(userDAO.findById(id)).thenReturn(Some(user))

    //when
    val invitation = invitationService.createInvitation(id)

    //then
    invitation should equal(message)
  }

  it should "save created invitation with correct sender and expiry date" in {
    //given
    val message = "some message"
    when(emailTemplateEngine.getTemplate(any[Templates.Template], any[Map[String, Object]])).thenReturn(EmailContentWithSubject(message, "subject"))
    when(userDAO.findById(id)).thenReturn(Some(user))

    //when
    invitationService.createInvitation(id)

    //then
    val invitationCaptor = ArgumentCaptor.forClass(classOf[Invitation])
    verify(invitationDAO).save(invitationCaptor.capture())
    invitationCaptor.getValue.invitationSender should be(id)
    invitationCaptor.getValue.expiryDate should be(clock.currentDateTimeUTC.plus(InvitationService.INVITATION_CODE_EXPIRATION_TIME))
  }

  it should "send email with invitation message" in {
    //given
    val message = "some message"
    when(userDAO.findById(id)).thenReturn(Some(user))
    when(emailTemplateEngine.getTemplate(any[Templates.Template], any[Map[String, Object]])).thenReturn(EmailContentWithSubject("subject", "some message"))

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
