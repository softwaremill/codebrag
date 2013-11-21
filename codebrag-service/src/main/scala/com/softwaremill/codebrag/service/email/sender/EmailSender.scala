package com.softwaremill.codebrag.service.email.sender

import com.typesafe.scalalogging.slf4j.Logging
import java.io.{ByteArrayOutputStream, ByteArrayInputStream}
import java.util.{Date, Properties}
import javax.activation.{DataHandler, DataSource}
import javax.mail.internet.{MimeBodyPart, MimeMultipart, MimeMessage, InternetAddress}
import javax.mail.{Address, Transport, Session, Message}

/**
 * Copied from softwaremill-common:
 * https://github.com/softwaremill/softwaremill-common/blob/master/softwaremill-sqs/src/main/java/com/softwaremill/common/sqs/email/EmailSender.java
 */
object EmailSender extends Logging {
  
  def supressSSLCertVerification(props: Properties, verifySSLCert: String) {
    if(verifySSLCert == "false") {
      props.put("mail.smtps.ssl.checkserveridentity", "false")
      props.put("mail.smtps.ssl.trust", "*")
    }
  }

  def send(smtpHost: String,
           smtpPort: String,
           smtpUsername: String,
           smtpPassword: String,
           verifySSLCertificate: String,
           from: String,
           encoding: String,
           emailDescription: EmailDescription,
           attachmentDescriptions: AttachmentDescription*) {

    val secured = smtpUsername != null

    // Setup mail server
    val props = new Properties()
    if (secured) {
      props.put("mail.smtps.host", smtpHost)
      props.put("mail.smtps.port", smtpPort)
      props.put("mail.smtps.starttls.enable", "true")
      props.put("mail.smtps.auth", "true")
      props.put("mail.smtps.user", smtpUsername)
      props.put("mail.smtps.password", smtpPassword)
      supressSSLCertVerification(props, verifySSLCertificate)
    } else {
      props.put("mail.smtp.host", smtpHost)
      props.put("mail.smtp.port", smtpPort)
    }

    // Get a mail session
    val session = Session.getInstance(props)

    val m = new MimeMessage(session)
    m.setFrom(new InternetAddress(from))

    val to = convertStringEmailsToAddresses(emailDescription.emails)
    val replyTo = convertStringEmailsToAddresses(emailDescription.replyToEmails)
    val cc = convertStringEmailsToAddresses(emailDescription.ccEmails)
    val bcc = convertStringEmailsToAddresses(emailDescription.bccEmails)

    m.setRecipients(javax.mail.Message.RecipientType.TO, to)
    m.setRecipients(Message.RecipientType.CC, cc)
    m.setRecipients(Message.RecipientType.BCC, bcc)
    m.setReplyTo(replyTo)
    m.setSubject(emailDescription.subject, encoding)
    m.setSentDate(new Date())

    if (attachmentDescriptions.length > 0) {
      addAttachments(m, emailDescription.message, encoding, attachmentDescriptions: _*)
    } else {
      m.setText(emailDescription.message, encoding, "plain")
    }

    if (secured) {
      val transport = session.getTransport("smtps")
      try {
        transport.connect(smtpUsername, smtpPassword)
        transport.sendMessage(m, m.getAllRecipients)
      } finally {
        transport.close()
      }
    } else {
      Transport.send(m)
    }

    logger.debug("Mail '" + emailDescription.subject + "' sent to: " + to.mkString(","))
  }

  def convertStringEmailsToAddresses(emails: Array[String]): Array[Address] = {
    val addresses = new Array[Address](emails.length)

    for (i <- 0 until emails.length) {
      addresses(i) = new InternetAddress(emails(i))
    }

    addresses
  }

  def addAttachments(mimeMessage: MimeMessage, msg: String, encoding: String,
    attachmentDescriptions: AttachmentDescription*) {
    val multiPart = new MimeMultipart()

    val textPart = new MimeBodyPart()
    multiPart.addBodyPart(textPart)
    textPart.setText(msg, encoding, "plain")

    for (attachmentDescription <- attachmentDescriptions) {
      val binaryPart = new MimeBodyPart()
      multiPart.addBodyPart(binaryPart)

      val ds = new DataSource() {
        def getInputStream = {
          new ByteArrayInputStream(attachmentDescription.content)
        }

        def getOutputStream = {
          val byteStream = new ByteArrayOutputStream()
          byteStream.write(attachmentDescription.content)
          byteStream
        }

        def getContentType = {
          attachmentDescription.contentType
        }

        def getName = {
          attachmentDescription.filename
        }
      }
      binaryPart.setDataHandler(new DataHandler(ds))
      binaryPart.setFileName(attachmentDescription.filename)
      binaryPart.setDescription("")
    }

    mimeMessage.setContent(multiPart)
  }
}
