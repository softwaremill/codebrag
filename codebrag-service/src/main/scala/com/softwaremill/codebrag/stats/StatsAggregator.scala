package com.softwaremill.codebrag.stats

import org.joda.time.DateTime
import com.softwaremill.codebrag.domain.InstanceSettings
import com.softwaremill.codebrag.stats.data.DailyStatistics
import com.softwaremill.codebrag.dao.instance.InstanceSettingsDAO
import com.softwaremill.codebrag.dao.finders.StatsEventsFinder

class StatsAggregator(val statsFinder: StatsEventsFinder, val instanceSettingsDao: InstanceSettingsDAO) {

  def getStatsForPreviousDayOf(date: DateTime): Either[String, DailyStatistics] = {
    getStatsFor(date.minusDays(1))
  }

  def getStatsFor(day: DateTime): Either[String, DailyStatistics] = {
    instanceSettingsDao.readOrCreate.right.map(getDailyStats(day, _))
  }


  private def getDailyStats(day: DateTime, instanceSettings: InstanceSettings): DailyStatistics = {
    val countersMap = Map(
      "commitsReviewedCount" -> statsFinder.reviewedCommitsCount(day),
      "commentsCount" -> statsFinder.commentsCount(day),
      "likesCount" -> statsFinder.likesCount(day),
      "registeredUsers" -> statsFinder.registeredUsersCount(day),
      "activeUsersCount" -> statsFinder.activeUsersCount(day)
    )
    DailyStatistics(instanceSettings.uniqueId, day, countersMap)
  }

}

