package com.softwaremill.codebrag.service.notification

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.service.email.{Email, EmailScheduler}
import com.softwaremill.codebrag.domain.User
import com.softwaremill.codebrag.service.templates.EmailTemplateEngine
import com.softwaremill.codebrag.service.templates.Templates._
import com.softwaremill.codebrag.service.config.CodebragConfig
import com.softwaremill.codebrag.dao.reporting.NotificationCountFinder

class NotificationService(emailScheduler: EmailScheduler, templateEngine: EmailTemplateEngine, codebragConfig: CodebragConfig, notificationCountFinder: NotificationCountFinder) extends Logging {

  def sendWelcomeNotification(user: User) {
    val noOfCommits = notificationCountFinder.getCounters(user.id).pendingCommitCount
    val context: Map[String, Any] = prepareContextForWelcomeNotification(user, noOfCommits)
    val template = templateEngine.getTemplate(WelcomeToCodebrag, context)
    emailScheduler.scheduleInstant(Email(user.email, template.subject, template.content))
  }


  private def prepareContextForWelcomeNotification(user: User, noOfCommits: Long): Map[String, Any] = {
    val users = Map("userName" -> user.name, "link" -> codebragConfig.applicationUrl)
    if (noOfCommits == 1) {
      return users ++ Map("noOfCommits" -> noOfCommits, "single" -> "true")
    }
    if (noOfCommits > 1) {
      return users ++ Map("noOfCommits" -> noOfCommits)
    }
    users
  }
}
