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
        LicenceEncryptor.decode(licence.value)
      }
      case None => {
        logger.debug("Licence key not found. Using trial licence")
        Licence.trialLicence(instanceId, licenceConfig.expiresInDays)
      }
    }
  }

}
