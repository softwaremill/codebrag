package com.softwaremill.codebrag.service.notification

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.service.email.{Email, EmailScheduler}
import com.softwaremill.codebrag.domain.User
import com.softwaremill.codebrag.service.templates.{EmailTemplates, TemplateEngine}
import com.softwaremill.codebrag.service.templates.EmailTemplates._
import com.softwaremill.codebrag.service.config.CodebragConfig
import com.softwaremill.codebrag.dao.reporting.NotificationCountFinder
import com.softwaremill.codebrag.common.Clock
import org.joda.time.format.DateTimeFormat

class NotificationService(emailScheduler: EmailScheduler, templateEngine: TemplateEngine, codebragConfig: CodebragConfig, notificationCountFinder: NotificationCountFinder, clock: Clock) extends Logging {

  import NotificationService.CountersToText.translate

  def sendWelcomeNotification(user: User) {
    val noOfCommits = notificationCountFinder.getCounters(user.id).pendingCommitCount
    val context: Map[String, Any] = prepareContextForWelcomeNotification(user, noOfCommits)
    val template = templateEngine.getEmailTemplate(WelcomeToCodebrag, context)
    emailScheduler.scheduleInstant(Email(List(user.email), template.subject, template.content))
  }


  def sendCommitsOrFollowupNotification(user: User, commitCount: Long, followupCount: Long) {
    val templateParams = Map(
      "username" -> user.name,
      "commit_followup_message" -> translate(commitCount, followupCount, isTotalCount = false),
      "application_url" -> codebragConfig.applicationUrl
    )
    val resolvedTemplate = templateEngine.getEmailTemplate(EmailTemplates.UserNotifications, templateParams)
    val email = Email(List(user.email), resolvedTemplate.subject, resolvedTemplate.content)
    emailScheduler.scheduleInstant(email)
  }

  def sendDailyDigest(user: User, commitCount: Long, followupCount: Long) {
    val templateParams = Map(
      "username" -> user.name,
      "commit_followup_message" -> translate(commitCount, followupCount, isTotalCount = true),
      "application_url" -> codebragConfig.applicationUrl,
      "date" -> clock.currentDateTime.toString(DateTimeFormat.forPattern("yyyy-MM-dd"))
    )
    val resolvedTemplate = templateEngine.getEmailTemplate(EmailTemplates.DailyDigest, templateParams)
    val email = Email(List(user.email), resolvedTemplate.subject, resolvedTemplate.content)
    emailScheduler.scheduleInstant(email)
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

object NotificationService {

    /**
    * Translates followups and commits numerical counters to text e.g.:
    * 1 commit and 2 followups
    * 1 new commit and 2 new followups
    */
  object CountersToText {

    def translate(commitCount: Long, followupCount: Long, isTotalCount: Boolean = false): String = {
      def newOrAll(word: String) = if(isTotalCount) word else s"new $word"
      val msg = {
        val commits = pluralize(commitCount, newOrAll("commit"))
        val followups = pluralize(followupCount, newOrAll("followup"))
        if (commitCount > 0 && followupCount > 0) s"$commits and $followups"
        else if (commitCount > 0) commits
        else followups
      }
      msg
    }

    private def pluralize(count: Long, singular: String)  = {
      val plural = singular + "s"
      if (count == 1) s"$count $singular" else s"$count $plural"
    }

  }

}
