package com.softwaremill.codebrag.service.templates

import org.fusesource.scalate._
import java.io.File

class EmailTemplateEngine {

  val TemplatesDirectory = "com/softwaremill/codebrag/service/templates/"

  val scalateEngine = new TemplateEngine(List(new File(TemplatesDirectory)), "production")

  def getTemplate(template: Templates.Template, params: Map[String, Any]): EmailContentWithSubject = {
    splitToContentAndSubject(prepareEmailTemplate(template, params))
  }

  private def prepareEmailTemplate(template: Templates.Template, params: Map[String, Any]): String = {
    scalateEngine.layout(TemplatesDirectory + template + ".mustache", params)
  }

  private[templates] def splitToContentAndSubject(template: String): EmailContentWithSubject = {
    // First line of template is used as an email subject, rest of the template goes to content
    val emailLines = template.split('\n')
    require(emailLines.length > 1, "Invalid email template. It should consist of at least two lines: one for subject and one for content")

    EmailContentWithSubject(emailLines.tail.mkString("\n"), emailLines.head)
  }
}

object Templates extends Enumeration {
  type Template = Value
  val WelcomeToCodebrag, Invitation, UserNotifications = Value

}