package com.softwaremill.codebrag.dao.finders

import org.scalatest.matchers.ShouldMatchers
import org.joda.time.{DateTimeZone, DateTime}
import com.softwaremill.codebrag.domain.reactions.CommitReviewedEvent
import org.scalatest.mock.MockitoSugar
import org.scalatest.FlatSpec
import org.mockito.Mockito._
import com.softwaremill.codebrag.dao.events.EventDAO

class StatsEventsFinderSpec extends FlatSpec with ShouldMatchers with MockitoSugar {

  val someDate        = new DateTime(2014, 2, 9, 19, 33, DateTimeZone.UTC)
  val someDateStart   = new DateTime(2014, 2, 9, 0, 0, 0, 0, DateTimeZone.UTC)
  val someDateEnd     = new DateTime(2014, 2, 9, 23, 59, 59, 999, DateTimeZone.UTC)

  it should "properly convert date to date bounds" in {
    // given
    val mockEventDAO = mock[EventDAO]
    val finder = new StatsEventsFinder(mockEventDAO)

    // when
    finder.reviewedCommitsCount(someDate)

    // then
    verify(mockEventDAO).countEvents(someDateStart, someDateEnd, CommitReviewedEvent.EventType)
  }
}
