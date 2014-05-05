package com.softwaremill.codebrag.licence

import org.joda.time.{Days, DateTime}
import com.softwaremill.codebrag.domain.InstanceId
import com.softwaremill.codebrag.common.Clock
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.licence.LicenceType.LicenceType

case class Licence(expirationDate: DateTime, maxUsers: Int, companyName: String, licenceType: LicenceType = LicenceType.Commercial) extends Logging {

  def valid(users: Int)(implicit clock: Clock) = {
    users <= maxUsers && !expirationDate.isBefore(clock.now)
  }

  def daysToExpire(implicit clock: Clock) = {
    val days = Days.daysBetween(clock.now.withTimeAtStartOfDay(), expirationDate).getDays
    if(days < 0) 0 else days
  }

  def toJsonString = LicenceAsJson.toJson(this)

}

object Licence {

  def apply(jsonString: String): Licence = LicenceAsJson.fromJson(jsonString)

  def trialLicence(instanceId: InstanceId, days: Int) = {
    val instanceCreationDate = new DateTime(instanceId.creationTime).withTimeAtStartOfDay()
    val licenceExpiryDate = instanceCreationDate.plusDays(days - 1).withTime(23, 59, 59, 999)
    Licence(licenceExpiryDate, 0, "-", LicenceType.Trial)
  }

}


object LicenceType extends Enumeration {
  type LicenceType = Value
  val Trial, Commercial = Value
}

