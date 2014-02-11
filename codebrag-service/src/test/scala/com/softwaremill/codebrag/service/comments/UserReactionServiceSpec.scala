package com.softwaremill.codebrag.service.comments

import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.dao.{LikeDAO, ObjectIdTestUtils}
import org.mockito.Mockito._
import org.mockito.Matchers._
import com.softwaremill.codebrag.domain._
import com.softwaremill.codebrag.service.comments.command.{IncomingLike, IncomingComment}
import com.softwaremill.codebrag.service.events.MockEventBus
import com.softwaremill.codebrag.domain.reactions.LikeEvent
import org.mockito.ArgumentCaptor
import com.softwaremill.codebrag.common.ClockSpec
import com.softwaremill.codebrag.dao.reaction.CommitCommentDAO

class UserReactionServiceSpec
  extends FlatSpec with MockitoSugar with ShouldMatchers with BeforeAndAfterEach with MockEventBus with ClockSpec {

  var userReactionService: UserReactionService = _
  var commentDaoMock: CommitCommentDAO = _
  var likeDaoMock: LikeDAO = _
  var likeValidatorMock: LikeValidator = _

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
    userReactionService = new UserReactionService(commentDaoMock, likeDaoMock, likeValidatorMock, eventBus)

    // make all likes to be first and valid
    when(likeValidatorMock.isLikeValid(any[Like])).thenReturn(Right())
  }

  it should "create a new comment for commit" in {
    // when
    userReactionService.storeComment(CommentForCommit)

    // then
    val commentArgument = ArgumentCaptor.forClass(classOf[Comment])
    verify(commentDaoMock).save(commentArgument.capture())
    commentArgument.getValue.commitId should equal(CommentForCommit.commitId)
    commentArgument.getValue.authorId should equal(CommentForCommit.authorId)
    commentArgument.getValue.message should equal(CommentForCommit.message)
  }

  it should "return created comment as a result" in {
    // when
    val savedComment = userReactionService.storeComment(CommentForCommit)

    // then
    savedComment.commitId should equal(CommentForCommit.commitId)
    savedComment.authorId should equal(CommentForCommit.authorId)
    savedComment.message should equal(CommentForCommit.message)
    savedComment.postingTime should equal(clock.nowUtc)
  }

  it should "return created inline comment as a result" in {
    // given

    // when
    val savedComment = userReactionService.storeComment(InlineCommentForCommit)

    // then
    savedComment.lineNumber should equal(InlineCommentForCommit.lineNumber)
    savedComment.fileName should equal(InlineCommentForCommit.fileName)
  }

  it should "create a new inline like" in {
    // when
    userReactionService.storeLike(InlineLikeForCommit)

    // then
    val likeArgument = ArgumentCaptor.forClass(classOf[Like])
    verify(likeDaoMock).save(likeArgument.capture())
    likeArgument.getValue.commitId should equal(InlineLikeForCommit.commitId)
    likeArgument.getValue.authorId should equal(InlineLikeForCommit.authorId)
  }

  it should "return created reaction" in {

    // when
    val savedComment = userReactionService.storeComment(CommentForCommit)
    val Right(savedLike) = userReactionService.storeLike(InlineLikeForCommit)

    // then
    savedComment.commitId should equal(CommentForCommit.commitId)
    savedComment.message should equal(CommentForCommit.message)

    savedLike.commitId should equal(InlineLikeForCommit.commitId)
    savedLike.lineNumber should equal(InlineLikeForCommit.lineNumber)
    savedLike.fileName should equal(InlineLikeForCommit.fileName)
  }

  it should "publish proper event after saving a 'like'" in {
    // when
    val Right(savedLike) = userReactionService.storeLike(InlineLikeForCommit)

    // then
    eventBus.getEvents.head should equal(LikeEvent(savedLike))
  }

  it should "not save another like for the same user and line of code" in {
    // given
    val msg = LikeValidator.UserCantLikeMultipleTimes
    when(likeValidatorMock.isLikeValid(any[Like])).thenReturn(Left(msg))

    // when
    val Left(secondSaveResult) = userReactionService.storeLike(InlineLikeForCommit)

    // then
    verifyZeroInteractions(likeDaoMock)
    secondSaveResult should be(msg)
  }

  it should "not save like for own code" in {
    // given
    val msg = LikeValidator.UserCantLikeOwnCode
    when(likeValidatorMock.isLikeValid(any[Like])).thenReturn(Left(msg))

    // when
    val Left(secondSaveResult) = userReactionService.storeLike(InlineLikeForCommit)

    // then
    verifyZeroInteractions(likeDaoMock)
    secondSaveResult should be(msg)
  }

  it should "not publish event when duplicate like not saved" in {
    // given
    userReactionService.storeLike(InlineLikeForCommit)
    when(likeValidatorMock.isLikeValid(any[Like])).thenReturn(Left("not valid"))
    eventBus.clear()

    // when
    userReactionService.storeLike(InlineLikeForCommit)

    // then
    eventBus.getEvents should be('empty)
  }

}
