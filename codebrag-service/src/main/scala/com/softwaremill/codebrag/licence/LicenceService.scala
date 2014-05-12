package com.softwaremill.codebrag.licence

import com.softwaremill.codebrag.common.Clock
import com.softwaremill.codebrag.domain.{LicenceKey, InstanceId}
import com.softwaremill.codebrag.dao.instance.InstanceParamsDAO
import java.util.concurrent.atomic.AtomicReference
import com.softwaremill.codebrag.dao.user.UserDAO

class LicenceService(
                      val instanceId: InstanceId,
                      val instanceParamsDao: InstanceParamsDAO,
                      val usersDao: UserDAO)(implicit clock: Clock) extends LicenceReader {

  private val currentLicence = new AtomicReference[Licence](readCurrentLicence())

  logLicenceInfo

  def interruptIfLicenceExpired {
    if(!licenceValid) {
      logger.debug(s"Licence expired at ${licenceExpiryDate}")
      throw new LicenceExpiredException
    }
  }

  def updateLicence(newLicence: Licence) {
    logger.debug(s"Updating licence")
    val encodedLicence = LicenceKey(LicenceEncryptor.encode(newLicence))
    instanceParamsDao.save(encodedLicence.toInstanceParam)
    currentLicence.set(newLicence)
    logLicenceInfo
    logger.debug(s"Licence updated")
  }

  def licenceValid = currentLicence.get.valid(usersDao.countAll().toInt)
  def licenceExpiryDate = currentLicence.get.expirationDate
  def daysToExpire = currentLicence.get.daysToExpire
  def licenceType = currentLicence.get.licenceType
  def companyName = currentLicence.get.companyName
  def maxUsers = currentLicence.get.maxUsers

  private def logLicenceInfo {
    logger.info(s"Licence valid: ${licenceValid}")
    logger.info(s"Expiration date: ${currentLicence.get.expirationDate}")
    logger.info(s"Licence expires in : ${daysToExpire} full days")
  }

}

