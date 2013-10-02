package com.softwaremill.codebrag.service.notification

import org.scalatest.FlatSpec
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.service.email.{Email, EmailScheduler}
import com.softwaremill.codebrag.service.templates.{EmailContentWithSubject, Templates, EmailTemplateEngine}
import com.softwaremill.codebrag.service.config.CodebragConfig
import com.softwaremill.codebrag.domain.User
import org.mockito.{Matchers, ArgumentCaptor, Mockito}
import Mockito._
import Matchers._
import com.softwaremill.codebrag.dao.reporting.NotificationCountFinder
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.reporting.views.NotificationCountersView

class NotificationServiceSepc extends FlatSpec with MockitoSugar with ShouldMatchers {

  it should "send welcome notification" in {
    //given
    val scheduler = mock[EmailScheduler]
    val engine = mock[EmailTemplateEngine]
    val config = mock[CodebragConfig]
    val countFinder = mock[NotificationCountFinder]
    val service = new NotificationService(scheduler, engine, config, countFinder)
    val emailAddress = "zuchos@zuchos.com"
    val user = User(null, null, "zuchos", emailAddress, null, null)

    when(engine.getTemplate(any[Templates.Template], any[Map[String, Object]])).thenReturn(EmailContentWithSubject("subject", "content"))
    when(countFinder.getCounters(any[ObjectId])).thenReturn(NotificationCountersView(10, 10))

    //when
    service.sendWelcomeNotification(user)

    //then
    val emailCaptor = ArgumentCaptor.forClass(classOf[Email])
    verify(scheduler).scheduleInstant(emailCaptor.capture())

    val email = emailCaptor.getValue
    email.address should equal(emailAddress)

    val templateCaptor = ArgumentCaptor.forClass(classOf[Templates.Template])
    verify(engine).getTemplate(templateCaptor.capture(), any[Map[String, Object]])
    templateCaptor.getValue should be(Templates.WelcomeToCodebrag)
  }

  private val templatesTest = Map("Currently there is nothing to review. We will notify you about new commits to review." -> 0,
    "There are 10 commits waiting for your to review at http://test:8080/#/commits." -> 10,
    "There is 1 commit waiting for your to review at http://test:8080/#/commits." -> 1)


  for (pair <- templatesTest) {

    "Email" should s"contain sentence '${pair._1}'" in {
      //given
      val scheduler = mock[EmailScheduler]
      val engine = new EmailTemplateEngine
      val config = mock[CodebragConfig]
      when(config.applicationUrl).thenReturn("http://test:8080")
      val countFinder = mock[NotificationCountFinder]
      val service = new NotificationService(scheduler, engine, config, countFinder)
      val emailAddress = "zuchos@zuchos.com"
      val user = User(null, null, "zuchos", emailAddress, null, null)

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
