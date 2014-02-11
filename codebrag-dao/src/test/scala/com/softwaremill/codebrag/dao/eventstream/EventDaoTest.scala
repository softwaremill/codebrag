package com.softwaremill.codebrag.dao.eventstream

import com.softwaremill.codebrag.test.{ClearMongoDataAfterTest, FlatSpecWithMongo}
import org.scalatest.matchers.ShouldMatchers
import org.joda.time.DateTime
import com.softwaremill.codebrag.domain.reactions.{LikeEvent, CommentAddedEvent, CommitReviewedEvent}
import com.softwaremill.codebrag.dao.events.NewUserRegistered
import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.{RealTimeClock, StatisticEvent}

class EventDaoTest extends FlatSpecWithMongo with ClearMongoDataAfterTest with ShouldMatchers {
  val dao = new EventDao

  val today = RealTimeClock.nowUtc
  val yesterday = today.minusDays(1)

  val CommitReviewedEventType = CommitReviewedEvent.EventType
  val CommentAddedEventType = CommentAddedEvent.EventType
  val LikeAddedEventType = LikeEvent.EventType
  val NewUserRegisteredEventType = NewUserRegistered.EventType

  it should "count events in the given date bounds" in {
    // given
    storeEventWith(today, CommitReviewedEventType)
    storeEventWith(today, CommitReviewedEventType)
    storeEventWith(yesterday, CommitReviewedEventType)

    // when
    val count1 = dao.countEvents(today, today, CommitReviewedEventType)
    val count2 = dao.countEvents(today.minusMinutes(1), today.plusMinutes(1), CommitReviewedEventType)
    val count3 = dao.countEvents(yesterday, today.plusMinutes(1), CommitReviewedEventType)

    // then
    count1 should be (2)
    count2 should be (2)
    count3 should be (3)
  }

  it should "count events of the given type" in {
    // given
    storeEventWith(today, LikeAddedEventType)
    storeEventWith(today, LikeAddedEventType)
    storeEventWith(yesterday, LikeAddedEventType)

    storeEventWith(today, CommentAddedEventType)
    storeEventWith(yesterday, CommentAddedEventType)

    // when
    val likesCount = dao.countEvents(yesterday, today, LikeAddedEventType)
    val commentsCount = dao.countEvents(yesterday, today, CommentAddedEventType)
    val reviewedCount = dao.countEvents(yesterday, today, CommitReviewedEventType)

    // then
    likesCount should be (3)
    commentsCount should be (2)
    reviewedCount should be (0)
  }

  it should "get number of active users (without registered)" in {
    // given
    storeEventWith(today, NewUserRegisteredEventType, Some(new ObjectId))
    storeEventWith(today, LikeAddedEventType, Some(new ObjectId))
    storeEventWith(today, CommentAddedEventType, Some(new ObjectId))

    // when
    val count = dao.countActiveUsers(today, today)

    // then
    count should be (2)
  }

  it should "count user as active only once" in {
    // given
    val user = new ObjectId
    storeEventWith(today, LikeAddedEventType, Some(user))
    storeEventWith(today, CommentAddedEventType, Some(user))

    // when
    val count = dao.countActiveUsers(today, today)

    // then
    count should be (1)

  }

  private def storeEventWith(_date: DateTime, _eventType: String, _userId: Option[ObjectId] = None) =
    dao.storeEvent(new StatisticEvent {
      def timestamp = _date
      def eventType = _eventType
      def userId = _userId
      def toEventStream = ""
    })
}
