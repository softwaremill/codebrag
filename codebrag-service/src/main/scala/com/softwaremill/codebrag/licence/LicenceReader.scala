package com.softwaremill.codebrag.licence

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.instance.InstanceParamsDAO
import com.softwaremill.codebrag.service.config.LicenceConfig
import com.softwaremill.codebrag.domain.{LicenceKey, InstanceId}

trait LicenceReader extends Logging {

  def instanceParamsDao: InstanceParamsDAO
  def licenceConfig: LicenceConfig
  def instanceId: InstanceId

  protected[licence] def readCurrentLicence() = {
    val existingLicence = instanceParamsDao.findByKey(LicenceKey.Key).map( param => LicenceKey(param.value))
    existingLicence match {
      case Some(licence) => {
        logger.debug("Found licence key. Trying to use one")
        buildLicenceOrFallbackToTrial(licence)
      }
      case None => {
        logger.debug("Licence key not found. Using trial licence")
        Licence.trialLicence(instanceId, licenceConfig.expiresInDays)
      }
    }
  }


  def buildLicenceOrFallbackToTrial(licenceKey: LicenceKey): Licence = {
    try {
      LicenceEncryptor.decode(licenceKey.value)
    } catch {
      case e: InvalidLicenceKeyException => {
        logger.debug("Could not read licence, falling back to trial licence")
        Licence.trialLicence(instanceId, licenceConfig.expiresInDays)
      }
    }
  }
}
