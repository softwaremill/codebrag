package pl.softwaremill.codebrag.service.schedulers

import pl.softwaremill.codebrag.service.templates.EmailContentWithSubject

trait EmailScheduler {

  def scheduleEmail(address: String, emailData: EmailContentWithSubject)

}
