package com.softwaremill.codebrag.service.notification

import org.scalatest.FlatSpec
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.service.email.{Email, EmailScheduler}
import com.softwaremill.codebrag.service.templates.{EmailContentWithSubject, EmailTemplates, TemplateEngine}
import com.softwaremill.codebrag.service.config.CodebragConfig
import org.mockito.{Matchers, ArgumentCaptor, Mockito}
import Mockito._
import Matchers._
import com.softwaremill.codebrag.dao.reporting.NotificationCountFinder
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.reporting.views.NotificationCountersView
import com.softwaremill.codebrag.domain.builder.UserAssembler
import com.softwaremill.codebrag.common.ClockSpec
import com.softwaremill.codebrag.service.notification

class NotificationServiceSpec
  extends FlatSpec with MockitoSugar with ShouldMatchers with ClockSpec {

  it should "send welcome notification" in {
    //given
    val scheduler = mock[EmailScheduler]
    val engine = mock[TemplateEngine]
    val config = mock[CodebragConfig]
    val countFinder = mock[NotificationCountFinder]
    val service = new NotificationService(scheduler, engine, config, countFinder, clock)
    val emailAddress = "sofo@sml.com"
    val user = UserAssembler.randomUser.get

    when(engine.getEmailTemplate(any[EmailTemplates.Value], any[Map[String, Object]])).thenReturn(EmailContentWithSubject("subject", "content"))
    when(countFinder.getCounters(any[ObjectId])).thenReturn(NotificationCountersView(10, 10))

    //when
    service.sendWelcomeNotification(user)

    //then
    val emailCaptor = ArgumentCaptor.forClass(classOf[Email])
    verify(scheduler).scheduleInstant(emailCaptor.capture())

    val email = emailCaptor.getValue
    email.addresses should equal(List(emailAddress))

    val templateCaptor = ArgumentCaptor.forClass(classOf[EmailTemplates.Value])
    verify(engine).getEmailTemplate(templateCaptor.capture(), any[Map[String, Object]])
    templateCaptor.getValue should be(EmailTemplates.WelcomeToCodebrag)
  }

  private val templatesTest = Map("Currently there is nothing to review. We will notify you about new commits to review." -> 0,
    "There are 10 commits waiting for you to review at http://test:8080/#/commits." -> 10,
    "There is 1 commit waiting for you to review at http://test:8080/#/commits." -> 1)


  for (pair <- templatesTest) {

    "Email" should s"contain sentence '${pair._1}'" in {
      //given
      val scheduler = mock[EmailScheduler]
      val engine = new TemplateEngine
      val config = mock[CodebragConfig]
      when(config.applicationUrl).thenReturn("http://test:8080")
      val countFinder = mock[NotificationCountFinder]
      val service = new NotificationService(scheduler, engine, config, countFinder, clock)
      val user = UserAssembler.randomUser.get

      when(countFinder.getCounters(any[ObjectId])).thenReturn(NotificationCountersView(pair._2, 10))

      //when
      service.sendWelcomeNotification(user)

      //then
      val emailCaptor = ArgumentCaptor.forClass(classOf[Email])
      verify(scheduler).scheduleInstant(emailCaptor.capture())

      val email = emailCaptor.getValue

      email.content should include(pair._1)
    }
  }

  it should "correctly translate counters to textual message" in {
    import NotificationService.CountersToText.translate

    verifyTranslation(commitsCount = 1, followupsCount = 1, isTotalCount = false)("1 new commit and 1 new followup")
    verifyTranslation(commitsCount = 1, followupsCount = 1, isTotalCount = true)("1 commit and 1 followup")
    verifyTranslation(commitsCount = 2, followupsCount = 2, isTotalCount = false)("2 new commits and 2 new followups")
    verifyTranslation(commitsCount = 2, followupsCount = 2, isTotalCount = true)("2 commits and 2 followups")

    def verifyTranslation(commitsCount: Long, followupsCount: Long, isTotalCount: Boolean)(expectedMsg: String) {
      translate(commitsCount, followupsCount, isTotalCount) should equal (expectedMsg)
    }
  }

}
