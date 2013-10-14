package com.softwaremill.codebrag.service.notification

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.service.email.{Email, EmailScheduler}
import com.softwaremill.codebrag.domain.User
import com.softwaremill.codebrag.service.templates.{Templates, EmailTemplateEngine}
import com.softwaremill.codebrag.service.templates.Templates._
import com.softwaremill.codebrag.service.config.CodebragConfig
import com.softwaremill.codebrag.dao.reporting.NotificationCountFinder

class NotificationService(emailScheduler: EmailScheduler, templateEngine: EmailTemplateEngine, codebragConfig: CodebragConfig, notificationCountFinder: NotificationCountFinder) extends Logging {

  def sendWelcomeNotification(user: User) {
    val noOfCommits = notificationCountFinder.getCounters(user.id).pendingCommitCount
    val context: Map[String, Any] = prepareContextForWelcomeNotification(user, noOfCommits)
    val template = templateEngine.getTemplate(WelcomeToCodebrag, context)
    emailScheduler.scheduleInstant(Email(List(user.email), template.subject, template.content))
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

  def sendCommitsOrFollowupNotification(user: User, commitCount: Long, followupCount: Long) {
    val subject = {
      val newCommits = if (commitCount == 1) s"$commitCount new commit" else s"$commitCount new commits"
      val newFollowups = if (followupCount == 1) s"$followupCount new followup" else s"$followupCount new followups"
      if (commitCount > 0 && followupCount > 0) s"$newCommits and $newFollowups"
      else if (commitCount > 0) newCommits
      else newFollowups
    }

    val templateParams = Map(
      "username" -> user.name,
      "commit_followup_message" -> subject,
      "application_url" -> codebragConfig.applicationUrl
    )
    val email = Email(List(user.email), subject, templateEngine.getTemplate(Templates.UserNotifications, templateParams).content)

    emailScheduler.scheduleInstant(email)
  }
}
