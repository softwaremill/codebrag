package com.softwaremill.codebrag.licence

import com.softwaremill.codebrag.common.Clock
import com.softwaremill.codebrag.domain.InstanceId
import com.softwaremill.codebrag.licence.LicenceType.LicenceType
import com.typesafe.scalalogging.slf4j.Logging
import org.joda.time.{DateTime, Days}

case class Licence(expirationDate: DateTime, maxUsers: Int, companyName: String, licenceType: LicenceType = LicenceType.Commercial) extends Logging {

  def valid(users: Int)(implicit clock: Clock) = isValidForUsersCount(users) && isValidForCurrentDate(clock)

  def isValidForUsersCount(users: Int) = users <= maxUsers

  def isValidForCurrentDate(implicit clock: Clock) = !expirationDate.isBefore(clock.now)

  def daysToExpire(implicit clock: Clock) = {
    val days = Days.daysBetween(clock.now.withTimeAtStartOfDay(), expirationDate).getDays
    if(days < 0) 0 else days
  }

  def toJsonString = LicenceAsJson.toJson(this)

}

object Licence {

  def apply(jsonString: String): Licence = LicenceAsJson.fromJson(jsonString)

  val TrialMaxUsersCount = 999
  val TrialExpirationDays = 30

  def trialLicence(instanceId: InstanceId) = {
    val instanceCreationDate = new DateTime(instanceId.creationTime).withTimeAtStartOfDay()
    val licenceExpiryDate = instanceCreationDate.plusDays(TrialExpirationDays - 1).withTime(23, 59, 59, 999)
    Licence(licenceExpiryDate, TrialMaxUsersCount, "-", LicenceType.Trial)
  }

}


object LicenceType extends Enumeration {
  type LicenceType = Value
  val Trial, Commercial = Value
}

