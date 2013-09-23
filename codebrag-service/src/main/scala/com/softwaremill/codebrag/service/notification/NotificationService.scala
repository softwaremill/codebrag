package com.softwaremill.codebrag.service.notification

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.service.email.{Email, EmailScheduler}
import com.softwaremill.codebrag.domain.User
import com.softwaremill.codebrag.service.templates.{EmailContentWithSubject, EmailTemplateEngine}
import com.softwaremill.codebrag.service.templates.Templates._
import com.softwaremill.codebrag.service.config.CodebragConfig

class NotificationService(emailScheduler: EmailScheduler, templateEngine: EmailTemplateEngine, codebragConfig: CodebragConfig) extends Logging {

  def sendWelcomeNotification(user: User) {
    //todo it would be nice to pass no, of commits to review (now it's 10, but it may change)
    val template = loadTemplate(WelcomeToCodebrag, Map("userName" -> user.name, "link" -> codebragConfig.applicationUrl))
    emailScheduler.scheduleInstant(Email(user.email, template.subject, template.content))
  }

  def loadTemplate(template: Template, context: Map[String, Object]): EmailContentWithSubject = {
    templateEngine.getTemplate(template, context)
  }
}
