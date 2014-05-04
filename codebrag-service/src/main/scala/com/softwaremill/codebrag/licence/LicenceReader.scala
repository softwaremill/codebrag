package com.softwaremill.codebrag.licence

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.instance.InstanceParamsDAO
import com.softwaremill.codebrag.service.config.LicenceConfig
import com.softwaremill.codebrag.domain.{InstanceLicence, InstanceId}

trait LicenceReader extends Logging {

  def instanceParamsDao: InstanceParamsDAO
  def licenceConfig: LicenceConfig
  def instanceId: InstanceId

  protected[licence] def readCurrentLicence() = {
    val existingLicence = instanceParamsDao.findByKey(InstanceLicence.Key).map( param => InstanceLicence(param.value))
    existingLicence match {
      case Some(licence) => {
        logger.debug("Found licence key. Trying to use one")
        Licence.decodeLicence(licence.value)
      }
      case None => {
        logger.debug("Licence key not found. Using trial licence")
        TrialLicenceBuilder.generate(instanceId, licenceConfig.expiresInDays)
      }
    }
  }

}
