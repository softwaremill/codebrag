package com.softwaremill.codebrag.licence

import com.softwaremill.codebrag.common.Clock
import com.softwaremill.codebrag.service.config.LicenceConfig
import com.softwaremill.codebrag.domain.InstanceId
import com.softwaremill.codebrag.dao.instance.InstanceParamsDAO

class LicenceService(val instanceId: InstanceId, val licenceConfig: LicenceConfig, val instanceParamsDao: InstanceParamsDAO)(implicit clock: Clock) extends LicenceReader {

  private val currentLicence = readCurrentLicence()

  logger.info(s"Licence valid: ${licenceValid}")
  logger.info(s"Expiration date: ${currentLicence.expirationDate}")
  logger.info(s"Licence expires in : ${daysToExpire} full days")

  def interruptIfLicenceExpired {
    if(!licenceValid) {
      logger.debug(s"Licence expired at ${licenceExpiryDate}")
      throw new LicenceExpiredException
    }
  }

  def licenceValid = currentLicence.valid
  def licenceExpiryDate = currentLicence.expirationDate
  def daysToExpire = currentLicence.daysToExpire
  def licenceType = currentLicence.licenceType
  def companyName = currentLicence.companyName

}

