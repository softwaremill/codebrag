package com.softwaremill.codebrag.dao.reporting

import com.softwaremill.codebrag.dao.eventstream.EventRecord
import com.foursquare.rogue.LiftRogue._
import org.joda.time.{DateTimeZone, DateTime}
import com.softwaremill.codebrag.domain.reactions.{LikeEvent, CommentAddedEvent, CommitReviewedEvent}
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.events.NewUserRegistered

class StatsEventsFinder extends Logging{

  private def dayBoundaries(day: DateTime) = {
    val day2 = day.toDateTime(DateTimeZone.UTC)
    val dayStart = day2.withTimeAtStartOfDay().toDateTime(DateTimeZone.UTC)
    val dayEnd = day2.withTime(23, 59, 59, 999).toDateTime(DateTimeZone.UTC)
    (dayStart, dayEnd)
  }

  def reviewedCommitsCount(day: DateTime): Long = {
    countOfEventsOfType(day, CommitReviewedEvent.EventType)
  }

  def registeredUsersCount(day: DateTime): Long = {
    countOfEventsOfType(day, NewUserRegistered.EventType)
  }

  def commentsCount(day: DateTime): Long = {
    countOfEventsOfType(day, CommentAddedEvent.EventType)
  }

  def likesCount(day: DateTime): Long = {
    countOfEventsOfType(day, LikeEvent.EventType)
  }

  def activeUsersCount(day: DateTime): Long = {
    val dateBounds = dayBoundaries(day)
    val allEventUsers = EventRecord.select(_.originatingUserId).where(_.date between(dateBounds._1, dateBounds._2)).and(_.eventType neqs NewUserRegistered.EventType).fetch()
    allEventUsers.toSet.size
  }

  private def countOfEventsOfType(day: DateTime, eventType: String) = {
    val dateBounds = dayBoundaries(day)
    val query = EventRecord.where(_.date between(dateBounds._1, dateBounds._2)).and(_.eventType eqs eventType)
    query.count()
  }

}
