package com.softwaremill.codebrag.licence

import com.softwaremill.codebrag.domain.InstanceId
import org.joda.time.DateTime

object TrialLicenceBuilder {

  def generate(instanceId: InstanceId, days: Int): LicenceDetails = {
    val instanceCreationDate = new DateTime(instanceId.creationTime).withTimeAtStartOfDay()
    val licenceExpiryDate = instanceCreationDate.plusDays(days - 1).withTime(23, 59, 59, 999)
    LicenceDetails(licenceExpiryDate, 0, "-", LicenceType.Trial)
  }

}
