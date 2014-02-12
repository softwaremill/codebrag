package com.softwaremill.codebrag.dao.finders

import org.joda.time.{DateTimeZone, DateTime}
import com.softwaremill.codebrag.domain.reactions.{LikeEvent, CommentAddedEvent, CommitReviewedEvent}
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.events.{EventDAO, NewUserRegistered}

class StatsEventsFinder(eventDAO: EventDAO) extends Logging{

  private def dayBoundaries(day: DateTime) = {
    val day2 = day.toDateTime(DateTimeZone.UTC)
    val dayStart = day2.withTimeAtStartOfDay().toDateTime(DateTimeZone.UTC)
    val dayEnd = day2.withTime(23, 59, 59, 999).toDateTime(DateTimeZone.UTC)
    (dayStart, dayEnd)
  }

  def reviewedCommitsCount(day: DateTime): Int = {
    countOfEventsOfType(day, CommitReviewedEvent.EventType)
  }

  def registeredUsersCount(day: DateTime): Int = {
    countOfEventsOfType(day, NewUserRegistered.EventType)
  }

  def commentsCount(day: DateTime): Int = {
    countOfEventsOfType(day, CommentAddedEvent.EventType)
  }

  def likesCount(day: DateTime): Int = {
    countOfEventsOfType(day, LikeEvent.EventType)
  }

  def activeUsersCount(day: DateTime): Int = {
    val dateBounds = dayBoundaries(day)
    eventDAO.countActiveUsers(dateBounds._1, dateBounds._2)
  }

  private def countOfEventsOfType(day: DateTime, eventType: String) = {
    val dateBounds = dayBoundaries(day)
    eventDAO.countEvents(dateBounds._1, dateBounds._2, eventType)
  }

}
