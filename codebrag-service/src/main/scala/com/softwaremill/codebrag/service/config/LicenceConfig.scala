package com.softwaremill.codebrag.service.config

import com.softwaremill.codebrag.common.config.ConfigWithDefault
import scala.concurrent.duration._

trait LicenceConfig extends ConfigWithDefault {

  lazy val expiresIn = getMilliseconds("codebrag.licence-expires-in", 30.days.toMillis).toInt

}


