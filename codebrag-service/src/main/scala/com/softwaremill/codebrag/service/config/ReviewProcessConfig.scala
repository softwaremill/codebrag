package com.softwaremill.codebrag.service.config

import com.softwaremill.codebrag.common.config.ConfigWithDefault

trait ReviewProcessConfig extends ConfigWithDefault {

  lazy val requiredReviewersCount = getInt("codebrag.required-reviewers-count", 1)

  lazy val initialCommitsToReviewCount = getInt("codebrag.initial-to-review", 10)
  lazy val initialToReviewDays = getInt("codebrag.initial-days-to-review", 30)

  lazy val daysToRecreateFollowups = getInt("codebrag.days-to-recreate-followups", 30)

}
