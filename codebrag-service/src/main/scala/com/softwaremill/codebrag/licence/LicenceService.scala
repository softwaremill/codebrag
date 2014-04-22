package com.softwaremill.codebrag.licence

import org.joda.time._
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.common.Clock
import com.softwaremill.codebrag.service.config.LicenceConfig
import com.softwaremill.codebrag.domain.InstanceSettings

class LicenceService(instanceSettings: InstanceSettings, licenceConfig: LicenceConfig, clock: Clock) extends Logging {

  logger.debug(s"Setting up licence check")

  private val date = new DateTime(instanceSettings.uniqueIdAsObjectId.getTime, DateTimeZone.UTC)
  val licenceExpiryDate = date.plusMillis(licenceConfig.expiresIn)

  logger.debug(s"Licence valid for this instance?: ${licenceValid}")
  logger.debug(s"Licence start date for this instance is: ${date}")
  logger.debug(s"Expiration date for this instance is: ${licenceExpiryDate}")
  logger.debug(s"Licence expires in : ${minutesToExpire} minutes")

  def interruptIfLicenceExpired {
    if(!licenceValid) {
      logger.debug(s"Licence expired at ${licenceExpiryDate}")
      throw new LicenceExpiredException
    }
  }
  
  def licenceValid = licenceExpiryDate.isAfter(clock.nowUtc)

  def minutesToExpire = Minutes.minutesBetween(clock.nowUtc, licenceExpiryDate).getMinutes

}
