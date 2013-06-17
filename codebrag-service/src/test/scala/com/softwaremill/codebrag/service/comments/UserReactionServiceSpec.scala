package com.softwaremill.codebrag.service.comments

import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.dao.{LikeDAO, ObjectIdTestUtils, CommitCommentDAO}
import pl.softwaremill.common.util.time.FixtureTimeClock
import org.mockito.Mockito._
import com.softwaremill.codebrag.domain._
import org.mockito.ArgumentCaptor
import com.softwaremill.codebrag.service.comments.command.IncomingComment

class UserReactionServiceSpec extends FlatSpec with MockitoSugar with ShouldMatchers with BeforeAndAfterEach {

  var userReactionService: UserReactionService = _
  var commentDaoMock: CommitCommentDAO = _
  var likeDaoMock: LikeDAO = _

  val FixedClock = new FixtureTimeClock(System.currentTimeMillis())

  val AuthorId = ObjectIdTestUtils.oid(100)
  val CommitId = ObjectIdTestUtils.oid(200)
  val CommentForCommit = IncomingComment(CommitId, AuthorId, "new comment message")
  val InlineCommentForCommit = IncomingComment(CommitId, AuthorId, "new inline comment message", Some("test_1.txt"), Some(20))

  override def beforeEach() {
    commentDaoMock = mock[CommitCommentDAO]
    likeDaoMock = mock[LikeDAO]
    userReactionService = new UserReactionService(commentDaoMock, likeDaoMock)(FixedClock)
  }

  it should "create a new comment for commit" in {
    // when
    userReactionService.storeUserReaction(CommentForCommit)

    // then
    val commentArgument = ArgumentCaptor.forClass(classOf[Comment])
    verify(commentDaoMock).save(commentArgument.capture())
    commentArgument.getValue.commitId should equal(CommentForCommit.commitId)
    commentArgument.getValue.authorId should equal(CommentForCommit.authorId)
    commentArgument.getValue.message should equal(CommentForCommit.message)
  }

  it should "return created comment as a result" in {
    // when
    val savedComment = userReactionService.storeUserReaction(CommentForCommit).asInstanceOf[Comment]

    // then
    savedComment.commitId should equal(CommentForCommit.commitId)
    savedComment.authorId should equal(CommentForCommit.authorId)
    savedComment.message should equal(CommentForCommit.message)
    savedComment.postingTime should equal(FixedClock.currentDateTimeUTC())
  }

  it should "return created inline comment as a result" in {
    // given

    // when
    val savedComment = userReactionService.storeUserReaction(InlineCommentForCommit).asInstanceOf[Comment]

    // then
    savedComment.lineNumber should equal(InlineCommentForCommit.lineNumber)
    savedComment.fileName should equal(InlineCommentForCommit.fileName)
  }

}
