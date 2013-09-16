package com.softwaremill.codebrag.service.email.sender

case class EmailDescription(emails: Array[String],
                            message: String,
                            subject: String,
                            replyToEmails: Array[String],
                            ccEmails: Array[String],
                            bccEmails: Array[String]) {

  def this(email: String, message: String, subject: String) =
    this(Array(email), message, subject, Array(), Array(), Array())
}
