package com.softwaremill.codebrag.service.invitations

import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.dao.{InvitationDAO}
import com.softwaremill.codebrag.service.email.{Email, EmailService}
import org.mockito.Mockito._
import org.mockito.Matchers._
import com.softwaremill.codebrag.domain.Invitation
import org.bson.types.ObjectId
import org.mockito.ArgumentCaptor
import com.softwaremill.codebrag.service.config.CodebragConfig
import com.softwaremill.codebrag.service.templates.{EmailContentWithSubject, EmailTemplates, TemplateEngine}
import com.softwaremill.codebrag.common.ClockSpec
import org.joda.time.Hours
import com.softwaremill.codebrag.domain.builder.UserAssembler
import com.softwaremill.codebrag.dao.user.UserDAO

class InvitationServiceSpec
  extends FlatSpec with MockitoSugar with ShouldMatchers with BeforeAndAfterEach with ClockSpec {

  private val code = "1234"
  val emails = List("some@some.some")
  val id = new ObjectId()
  val userName = "zuchos"
  val user = UserAssembler.randomUser.withId(id).withFullName(userName).get
  val config = mock[CodebragConfig]

  var invitationDAO: InvitationDAO = _
  var userDAO: UserDAO = _
  var emailService: EmailService = _
  var emailTemplateEngine: TemplateEngine = _
  var uniqueHashGenerator: UniqueHashGenerator = _

  var invitationService: InvitationService = _

  private val appPath = "http://localhost:8080"
  private val registerUrlBase = appPath + "/#/register/"
  when(config.applicationUrl).thenReturn(appPath)

  override def beforeEach() {
    invitationDAO = mock[InvitationDAO]
    userDAO = mock[UserDAO]
    emailService = mock[EmailService]
    emailTemplateEngine = mock[TemplateEngine]
    uniqueHashGenerator = mock[UniqueHashGenerator]
    invitationService = new InvitationService(invitationDAO, userDAO, emailService, config, uniqueHashGenerator, emailTemplateEngine)(clock)
  }

  it should "positively verify invitation" in {
    //given
    val expirationInFuture = clock.nowUtc.plusHours(1)
    when(invitationDAO.findByCode(code)).thenReturn(Some(Invitation(code, new ObjectId(), expirationInFuture)))

    //when
    val verify = invitationService.verify(code)

    //then
    verify should be(true)
  }

  it should "negatively verify invitation when invitation expired" in {
    //given
    val expirationTimeInThePast = clock.nowUtc.minusHours(1)
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
    invitationCaptor.getValue.expiryDate should be(clock.nowUtc.plus(config.invitationExpiryTime))
  }

  it should "send email with invitation message" in {
    //given
    val invitationLink = "inv code"
    when(userDAO.findById(id)).thenReturn(Some(user))
    when(emailTemplateEngine.getEmailTemplate(any[EmailTemplates.Value], any[Map[String, Object]])).thenReturn(EmailContentWithSubject(invitationLink, "subject"))

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
