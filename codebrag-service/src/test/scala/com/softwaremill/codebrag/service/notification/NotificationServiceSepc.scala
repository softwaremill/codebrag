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

class NotificationServiceSepc extends FlatSpec with MockitoSugar with ShouldMatchers {

  it should "send welcome notification" in {
    //given
    val scheduler = mock[EmailScheduler]
    val engine = mock[EmailTemplateEngine]
    val config = mock[CodebragConfig]
    val service = new NotificationService(scheduler, engine, config)
    val emailAddress = "zuchos@zuchos.com"
    val user = new User(null, null, "zuchos", emailAddress, null, null)

    when(engine.getTemplate(any[Templates.Template], any[Map[String, Object]])).thenReturn(EmailContentWithSubject("subject", "content"))

    //when
    service.sendWelcomeNotification(user)

    //then
    val emailCaptor = ArgumentCaptor.forClass(classOf[Email])
    verify(scheduler).scheduleInstant(emailCaptor.capture())

    val email = emailCaptor.getValue
    email.address should be eq emailAddress

    val templateCaptor = ArgumentCaptor.forClass(classOf[Templates.Template])
    verify(engine).getTemplate(templateCaptor.capture(), any[Map[String, Object]])
    templateCaptor.getValue should be(Templates.WelcomeToCodebrag)
  }

}
