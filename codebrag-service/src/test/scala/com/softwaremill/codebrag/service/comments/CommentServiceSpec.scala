package com.softwaremill.codebrag.service.comments

import command.AddComment
import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.common.FakeIdGenerator
import com.softwaremill.codebrag.dao.{ObjectIdTestUtils, CommitCommentDAO}
import pl.softwaremill.common.util.time.FixtureTimeClock
import org.mockito.Mockito._
import com.softwaremill.codebrag.domain._
import org.joda.time.{DateTimeZone, DateTime}
import org.bson.types.ObjectId
import org.mockito.ArgumentCaptor

class CommentServiceSpec extends FlatSpec with MockitoSugar with ShouldMatchers with BeforeAndAfterEach {

  var commentService: CommentService = _
  var commentDaoMock: CommitCommentDAO = _

  val FixedClock = new FixtureTimeClock(System.currentTimeMillis())

  val AuthorId = ObjectIdTestUtils.oid(100)
  val CommitId = ObjectIdTestUtils.oid(200)
  val NewComment = AddComment(CommitId, AuthorId, "new comment message")

  override def beforeEach() {
    commentDaoMock = mock[CommitCommentDAO]
    commentService = new CommentService(commentDaoMock)(FixedClock)
  }

  it should "create a new commit and call dao to persist it" in {
    // when
    commentService.addCommentToCommit(NewComment)

    // then
    val commentArgument = ArgumentCaptor.forClass(classOf[CommitComment])
    verify(commentDaoMock).save(commentArgument.capture())
    commentArgument.getValue.commitId should equal(NewComment.commitId)
    commentArgument.getValue.authorId should equal(NewComment.authorId)
    commentArgument.getValue.message should equal(NewComment.message)
  }

  it should "return new comment as a result" in {
    // when
    val savedComment = commentService.addCommentToCommit(NewComment)

    // then
    savedComment.commitId should equal(NewComment.commitId)
    savedComment.authorId should equal(NewComment.authorId)
    savedComment.message should equal(NewComment.message)
    savedComment.postingTime should equal(FixedClock.currentDateTimeUTC())
  }

}
