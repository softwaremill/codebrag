package com.softwaremill.codebrag.service.schedulers

import com.softwaremill.codebrag.service.templates.EmailContentWithSubject

trait EmailScheduler {

  def scheduleEmail(address: String, emailData: EmailContentWithSubject)

}
