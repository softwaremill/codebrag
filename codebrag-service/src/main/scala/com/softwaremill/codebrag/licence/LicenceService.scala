package com.softwaremill.codebrag.licence

import org.joda.time.{DateTimeZone, DateTime}
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.domain.InstanceSettings

class LicenceService(instanceSettings: InstanceSettings) extends Logging {

  def assertLicenceValid {
    logger.debug(s"Instance date is ${date}")
    val expired = date.withTimeAtStartOfDay().plusDays(30).isBeforeNow
    logger.debug(s"Expired? ${expired}")
    if(expired) throw new LicenceExpiredException
  }

  private lazy val date = {
    new DateTime(instanceSettings.uniqueIdAsObjectId.getTime, DateTimeZone.UTC)
  }

}
