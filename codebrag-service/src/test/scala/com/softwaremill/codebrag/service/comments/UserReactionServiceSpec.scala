package com.softwaremill.codebrag.service.comments

import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.dao.{LikeDAO, ObjectIdTestUtils, CommitCommentDAO}
import pl.softwaremill.common.util.time.FixtureTimeClock
import org.mockito.Mockito._
import org.mockito.Matchers._
import com.softwaremill.codebrag.domain._
import com.softwaremill.codebrag.service.comments.command.{IncomingLike, IncomingComment}
import com.softwaremill.codebrag.service.events.FakeEventBus
import com.softwaremill.codebrag.domain.reactions.CommitLiked
import org.mockito.ArgumentCaptor

class UserReactionServiceSpec extends FlatSpec with MockitoSugar with ShouldMatchers with BeforeAndAfterEach with FakeEventBus {

  var userReactionService: UserReactionService = _
  var commentDaoMock: CommitCommentDAO = _
  var likeDaoMock: LikeDAO = _
  var likeValidatorMock: LikeValidator = _

  val FixedClock = new FixtureTimeClock(System.currentTimeMillis())

  val AuthorId = ObjectIdTestUtils.oid(100)
  val CommitId = ObjectIdTestUtils.oid(200)
  val CommentForCommit = IncomingComment(CommitId, AuthorId, "new comment message")
  val InlineCommentForCommit = IncomingComment(CommitId, AuthorId, "new inline comment message", Some("test_1.txt"), Some(20))
  val InlineLikeForCommit = IncomingLike(CommitId, AuthorId, Some("test_1.txt"), Some(20))

  override def beforeEach() {
    eventBus.clear()
    commentDaoMock = mock[CommitCommentDAO]
    likeDaoMock = mock[LikeDAO]
    likeValidatorMock = mock[LikeValidator]
    userReactionService = new UserReactionService(commentDaoMock, likeDaoMock, likeValidatorMock, eventBus)(FixedClock)

    // make all likes to be first and valid
    when(likeValidatorMock.isLikeValid(any[Like])).thenReturn(Right(true))
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

  it should "create a new inline like" in {
    // when
    userReactionService.storeUserReaction(InlineLikeForCommit)

    // then
    val likeArgument = ArgumentCaptor.forClass(classOf[Like])
    verify(likeDaoMock).save(likeArgument.capture())
    likeArgument.getValue.commitId should equal(InlineLikeForCommit.commitId)
    likeArgument.getValue.authorId should equal(InlineLikeForCommit.authorId)
  }

  it should "return created reaction" in {

    // when
    val savedComment = userReactionService.storeUserReaction(CommentForCommit)
    val Right(savedLike) = userReactionService.storeUserReaction(InlineLikeForCommit)

    // then
    savedComment.commitId should equal(CommentForCommit.commitId)
    savedComment.message should equal(CommentForCommit.message)

    savedLike.commitId should equal(InlineLikeForCommit.commitId)
    savedLike.lineNumber should equal(InlineLikeForCommit.lineNumber)
    savedLike.fileName should equal(InlineLikeForCommit.fileName)
  }

  it should "publish proper event after saving a 'like'" in {
    // when
    val Right(savedLike) = userReactionService.storeUserReaction(InlineLikeForCommit)

    // then
    eventBus.getEvents.head should equal(CommitLiked(savedLike))
  }

  it should "not save another like for the same user and line of code" in {
    // given
    val msg = LikeValidator.UserCantLikeMultipleTimes
    when(likeValidatorMock.isLikeValid(any[Like])).thenReturn(Left(msg))

    // when
    val Left(secondSaveResult) = userReactionService.storeUserReaction(InlineLikeForCommit)

    // then
    verifyZeroInteractions(likeDaoMock)
    secondSaveResult should be(msg)
  }

  it should "not save like for own code" in {
    // given
    val msg = LikeValidator.UserCantLikeOwnCode
    when(likeValidatorMock.isLikeValid(any[Like])).thenReturn(Left(msg))

    // when
    val Left(secondSaveResult) = userReactionService.storeUserReaction(InlineLikeForCommit)

    // then
    verifyZeroInteractions(likeDaoMock)
    secondSaveResult should be(msg)
  }

  it should "not publish event when duplicate like not saved" in {
    // given
    userReactionService.storeUserReaction(InlineLikeForCommit)
    when(likeValidatorMock.isLikeValid(any[Like])).thenReturn(Left("not valid"))
    eventBus.clear()

    // when
    userReactionService.storeUserReaction(InlineLikeForCommit)

    // then
    eventBus.getEvents should be('empty)
  }

}
