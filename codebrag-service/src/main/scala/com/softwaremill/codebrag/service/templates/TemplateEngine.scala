package com.softwaremill.codebrag.service.templates

import java.io.File

class TemplateEngine {

  val TemplatesDirectory = "com/softwaremill/codebrag/service/templates/"

  val scalateEngine = new org.fusesource.scalate.TemplateEngine(List(new File(TemplatesDirectory)), "production")

  def getPlainTextTemplate(template: PlainTextTemplates.Value, params: Map[String, Any]): String= {
    scalateEngine.layout(TemplatesDirectory + template + ".mustache", params)
  }

  def getEmailTemplate(template: EmailTemplates.Value, params: Map[String, Any]): EmailContentWithSubject = {
    splitToContentAndSubject(prepareEmailTemplate(template, params))
  }

  private def prepareEmailTemplate(template: EmailTemplates.Value, params: Map[String, Any]): String = {
    scalateEngine.layout(TemplatesDirectory + template + ".mustache", params)
  }

  private[templates] def splitToContentAndSubject(template: String): EmailContentWithSubject = {
    // First line of template is used as an email subject, rest of the template goes to content
    val emailLines = template.split('\n')
    require(emailLines.length > 1, "Invalid email template. It should consist of at least two lines: one for subject and one for content")

    EmailContentWithSubject(emailLines.tail.mkString("\n"), emailLines.head)
  }
}

object EmailTemplates extends Enumeration {
  type EmailTemplates = Value
  val WelcomeToCodebrag, Invitation, FollowupNotifications, FollowupAndCommitNotifications, DailyDigest = Value
}

object PlainTextTemplates extends Enumeration {
  type PlainTextTemplates = Value
  val WelcomeComment = Value
}