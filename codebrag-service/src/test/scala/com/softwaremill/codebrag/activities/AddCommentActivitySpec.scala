package com.softwaremill.codebrag.activities

import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.service.comments.UserReactionService
import com.softwaremill.codebrag.service.followups.FollowupService
import com.softwaremill.codebrag.common.{ClockSpec, EventBus}
import com.softwaremill.codebrag.service.comments.command.IncomingComment
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.Comment
import org.mockito.Mockito._
import com.softwaremill.codebrag.domain.reactions.CommentAddedEvent

class AddCommentActivitySpec
  extends FlatSpec with MockitoSugar with ShouldMatchers with BeforeAndAfterEach with ClockSpec {

  var userReactionService: UserReactionService = _
  var followupService: FollowupService = _
  var eventBus: EventBus = _

  var activity: AddCommentActivity = _

  override def beforeEach() {
    userReactionService = mock[UserReactionService]
    followupService = mock[FollowupService]
    eventBus = mock[EventBus]

    activity = new AddCommentActivity(userReactionService, followupService, eventBus)
  }

  it should "generate commit added event" in {
    // given
    val commitId: ObjectId = ObjectId.get()
    val authorId: ObjectId = ObjectId.get()
    val message: String = "Comment"
    
    val newComment = IncomingComment(commitId, authorId, message)
    val expected = Comment(ObjectId.get(), commitId, authorId, clock.currentDateTime, message)
    
    when(userReactionService.storeComment(newComment)).thenReturn(expected)
    
    // when
    val actual = activity.addCommentToCommit(newComment)
    
    // then
    verify(userReactionService).storeComment(newComment)
    actual should equal(expected)
    verify(followupService).generateFollowupsForComment(expected)
    verify(eventBus).publish(CommentAddedEvent(actual))
  }

}
