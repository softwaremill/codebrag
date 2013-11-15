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
import com.softwaremill.codebrag.common.FixtureTimeClock
import org.joda.time.DateTime

class NotificationServiceSpec extends FlatSpec with MockitoSugar with ShouldMatchers {

  val clock = new FixtureTimeClock(DateTime.now.getMillis.toInt)

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
    "There are 10 commits waiting for your to review at http://test:8080/#/commits." -> 10,
    "There is 1 commit waiting for your to review at http://test:8080/#/commits." -> 1)


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

}
