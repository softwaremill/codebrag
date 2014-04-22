package com.softwaremill.codebrag.licence

import org.joda.time._
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.common.Clock
import com.softwaremill.codebrag.service.config.LicenceConfig
import com.softwaremill.codebrag.domain.InstanceSettings

class LicenceService(instanceSettings: InstanceSettings, licenceConfig: LicenceConfig, clock: Clock) extends Logging {

  logger.debug(s"Setting up licence check")
  private val date = new DateTime(instanceSettings.uniqueIdAsObjectId.getTime).withTimeAtStartOfDay()
  val licenceExpiryDate = date.plusDays(licenceConfig.expiresInDays - 1).withTime(23, 59, 59, 999)

  logger.debug(s"Licence valid?: ${licenceValid}")
  logger.debug(s"Licence countdown start date: ${date}")
  logger.debug(s"Expiration date: ${licenceExpiryDate}")
  logger.debug(s"Licence expires in : ${daysToExpire} full days")

  def interruptIfLicenceExpired {
    if(!licenceValid) {
      logger.debug(s"Licence expired at ${licenceExpiryDate}")
      throw new LicenceExpiredException
    }
  }
  
  def licenceValid = licenceExpiryDate.isAfter(clock.now)

  def daysToExpire = {
    val days = Days.daysBetween(clock.now.withTimeAtStartOfDay(), licenceExpiryDate).getDays
    if(days < 0) 0 else days
  }

}