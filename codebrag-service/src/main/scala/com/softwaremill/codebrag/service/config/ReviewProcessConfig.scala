package com.softwaremill.codebrag.service.config

import com.softwaremill.codebrag.common.config.ConfigWithDefault
import com.typesafe.config.Config

trait ReviewProcessConfig extends ConfigWithDefault {
  lazy val requiredReviewersCount = ReviewersCount.getFrom(rootConfig, "codebrag.required-reviewers-count")
  lazy val initialCommitsToReviewCount = getInt("codebrag.initial-to-review", 10)
  lazy val initialToReviewDays = getInt("codebrag.initial-days-to-review", 30)

  lazy val daysToRecreateFollowups = getInt("codebrag.days-to-recreate-followups", 30)
}

object ReviewersCount {
  private val AllValue = Int.MaxValue
  private val DefaultValue = 1

  def getFrom(config: Config, key: String): Int = {
    if(config.hasPath(key)) {
      config.getValue(key).unwrapped() match {
        case i: Integer => i
        case s: String => AllValue
        case _ => DefaultValue
      }
    } else {
      DefaultValue
    }
  }
}