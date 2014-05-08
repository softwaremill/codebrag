package com.softwaremill.codebrag.stats

import org.joda.time.DateTime
import com.softwaremill.codebrag.domain.InstanceId
import com.softwaremill.codebrag.stats.data.DailyStatistics
import com.softwaremill.codebrag.dao.finders.StatsEventsFinder
import com.softwaremill.codebrag.service.config.StatsConfig

class StatsAggregator(val statsFinder: StatsEventsFinder, val instanceId: InstanceId, val statsConfig: StatsConfig) {

  def getStatsForPreviousDayOf(date: DateTime): DailyStatistics = {
    getStatsFor(date.minusDays(1))
  }

  private def getStatsFor(day: DateTime): DailyStatistics = {
    val countersMap = Map(
      "commitsReviewedCount" -> statsFinder.reviewedCommitsCount(day),
      "commentsCount" -> statsFinder.commentsCount(day),
      "likesCount" -> statsFinder.likesCount(day),
      "registeredUsers" -> statsFinder.registeredUsersCount(day),
      "activeUsersCount" -> statsFinder.activeUsersCount(day)
    )
    DailyStatistics(instanceId.value, statsConfig.appVersion, day, countersMap)
  }

}

