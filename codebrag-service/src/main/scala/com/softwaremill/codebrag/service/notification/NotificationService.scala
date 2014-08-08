package com.softwaremill.codebrag.service.notification

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.service.email.{Email, EmailScheduler}
import com.softwaremill.codebrag.domain.User
import com.softwaremill.codebrag.service.templates.{EmailTemplates, TemplateEngine}
import com.softwaremill.codebrag.service.templates.EmailTemplates._
import com.softwaremill.codebrag.service.config.CodebragConfig
import com.softwaremill.codebrag.common.Clock
import org.joda.time.format.DateTimeFormat
import com.softwaremill.codebrag.dao.finders.followup.FollowupFinder
import com.softwaremill.codebrag.finders.commits.toreview.ToReviewCommitsFinder
import com.softwaremill.codebrag.usecases.notifications.UserNotificationsView

class NotificationService(
  emailScheduler: EmailScheduler,
  templateEngine: TemplateEngine,
  codebragConfig: CodebragConfig,
  toReviewCommitsFinder: ToReviewCommitsFinder,
  clock: Clock) extends Logging {

  import NotificationService.CountersToText.translate

  def sendWelcomeNotification(user: User) {
    val noOfCommits = toReviewCommitsFinder.countForUserRepoAndBranch(user.id)
    val context: Map[String, Any] = prepareContextForWelcomeNotification(user, noOfCommits)
    val template = templateEngine.getEmailTemplate(WelcomeToCodebrag, context)
    emailScheduler.scheduleInstant(Email(List(user.emailLowerCase), template.subject, template.content))
  }


  def sendFollowupNotification(user: User, followupCount: Long) {
    val templateParams = Map(
      "username" -> user.name,
      "commit_followup_message" -> translate(0, followupCount, isTotalCount = false),
      "application_url" -> codebragConfig.applicationUrl
    )
    val resolvedTemplate = templateEngine.getEmailTemplate(EmailTemplates.UserNotifications, templateParams)
    val email = Email(List(user.emailLowerCase), resolvedTemplate.subject, resolvedTemplate.content)
    emailScheduler.scheduleInstant(email)
  }

  def sendDailySummary(user: User, toReviewSummary: UserNotificationsView) {
    val templateParams = Map(
      "username" -> user.name,
      "summaryMessage" -> translate(toReviewSummary.repos.foldLeft(0)(_ + _.commits), toReviewSummary.followups, isTotalCount = true),
      "commitsNotifications" -> toReviewSummary.repos,
      "applicationUrl" -> codebragConfig.applicationUrl,
      "date" -> clock.now.toString(DateTimeFormat.forPattern("yyyy-MM-dd"))
    )
    val resolvedTemplate = templateEngine.getEmailTemplate(EmailTemplates.DailyDigest, templateParams)
    val email = Email(List(user.emailLowerCase), resolvedTemplate.subject, resolvedTemplate.content)
    emailScheduler.scheduleInstant(email)
  }

  private def prepareContextForWelcomeNotification(user: User, noOfCommits: Long): Map[String, Any] = {
    val params = Map("userName" -> user.name, "link" -> codebragConfig.applicationUrl)
    if (noOfCommits == 1) {
      return params ++ Map("noOfCommits" -> noOfCommits, "single" -> "true")
    }
    if (noOfCommits > 1) {
      return params ++ Map("noOfCommits" -> noOfCommits)
    }
    params
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
