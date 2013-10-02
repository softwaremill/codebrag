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
import org.joda.time.Hours

class InvitationServiceSpec extends FlatSpec with MockitoSugar with ShouldMatchers with BeforeAndAfterEach {

  private val code = "1234"
  val emails = List("some@some.some")
  val id = new ObjectId()
  val userName = "zuchos"
  val user = User(id, null, userName, null, null, null)
  val config = mock[CodebragConfig]

  val clock = new FixtureTimeClock(System.currentTimeMillis())
  var invitationDAO: InvitationDAO = _
  var userDAO: UserDAO = _
  var emailService: EmailService = _
  var emailTemplateEngine: EmailTemplateEngine = _
  var uniqueHashGenerator: UniqueHashGenerator = _

  var invitationService: InvitationService = _

  private val appPath = "http://localhost:8080"
  private val registerUrlBase = appPath + "/#/register/"
  when(config.applicationUrl).thenReturn(appPath)

  override def beforeEach() {
    invitationDAO = mock[InvitationDAO]
    userDAO = mock[UserDAO]
    emailService = mock[EmailService]
    emailTemplateEngine = mock[EmailTemplateEngine]
    uniqueHashGenerator = mock[UniqueHashGenerator]
    invitationService = new InvitationService(invitationDAO, userDAO, emailService, config, uniqueHashGenerator, emailTemplateEngine)(clock)
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
    val regCode = "123123123"
    when(userDAO.findById(id)).thenReturn(Some(user))
    when(uniqueHashGenerator.generateUniqueHashCode()).thenReturn(regCode)

    //when
    val invitation = invitationService.createInvitationLink(id)

    //then
    invitation should equal(registerUrlBase + regCode)
  }

  it should "save created invitation with correct sender and expiry date" in {
    //given
    val regCode = "123123123"
    when(config.invitationExpiryTime).thenReturn(Hours.hours(20))
    when(userDAO.findById(id)).thenReturn(Some(user))
    when(uniqueHashGenerator.generateUniqueHashCode()).thenReturn(regCode)

    //when
    invitationService.createInvitationLink(id)

    //then
    val invitationCaptor = ArgumentCaptor.forClass(classOf[Invitation])
    verify(invitationDAO).save(invitationCaptor.capture())
    invitationCaptor.getValue.invitationSender should be(id)
    invitationCaptor.getValue.expiryDate should be(clock.currentDateTimeUTC.plus(config.invitationExpiryTime))
  }

  it should "send email with invitation message" in {
    //given
    val invitationLink = "inv code"
    when(userDAO.findById(id)).thenReturn(Some(user))
    when(emailTemplateEngine.getTemplate(any[Templates.Template], any[Map[String, Object]])).thenReturn(EmailContentWithSubject(invitationLink, "subject"))

    //when
    invitationService.sendInvitation(emails, invitationLink, id)

    //then
    val argumentCaptor = ArgumentCaptor.forClass(classOf[Email])
    verify(emailService).send(argumentCaptor.capture())

    val emailArgument = argumentCaptor.getValue
    emailArgument.addresses should be(emails)
    emailArgument.content should be(invitationLink)
  }


}
