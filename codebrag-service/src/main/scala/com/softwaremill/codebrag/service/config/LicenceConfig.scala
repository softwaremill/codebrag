package com.softwaremill.codebrag.service.config

import com.softwaremill.codebrag.common.config.ConfigWithDefault

trait LicenceConfig extends ConfigWithDefault {

  lazy val expiresInDays = getInt("codebrag.licence-expiration-days", 45)

}