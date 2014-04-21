package com.softwaremill.codebrag.licence

import org.joda.time.{DateTimeZone, DateTime}
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.domain.InstanceSettings
import com.softwaremill.codebrag.common.Clock

class LicenceService(instanceSettings: InstanceSettings, clock: Clock) extends Logging {

  def interruptIfLicenceExpired {
    if(!licenceValid) {
      logger.debug(s"Licence expired at ${licenceExpiryDate}")
      throw new LicenceExpiredException
    }
  }
  
  def licenceValid = licenceExpiryDate.isAfter(clock.nowUtc)

  lazy val licenceExpiryDate = date.withTimeAtStartOfDay().plusDays(30)

  private lazy val date = {
    new DateTime(instanceSettings.uniqueIdAsObjectId.getTime, DateTimeZone.UTC)
  }

}
