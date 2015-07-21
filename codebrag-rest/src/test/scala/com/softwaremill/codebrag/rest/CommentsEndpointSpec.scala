package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.AuthenticatableServletSpec
import com.softwaremill.codebrag.dao.finders.views.CommentView
import com.softwaremill.codebrag.domain._
import com.softwaremill.codebrag.service.comments.command.IncomingComment
import com.softwaremill.codebrag.service.user.Authenticator
import com.softwaremill.codebrag.usecases.reactions.AddCommentUseCase
import org.bson.types.ObjectId
import org.joda.time.DateTime
import org.mockito.ArgumentCaptor
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatra.auth.Scentry


class CommentsEndpointSpec extends AuthenticatableServletSpec with BeforeAndAfterEach {

  var addCommentUseCase: AddCommentUseCase = _

  val user = currentUser(new ObjectId)
  val commitId = new ObjectId

  override def beforeEach {
    super.beforeEach
    addCommentUseCase = mock[AddCommentUseCase]
    addServlet(new TestableCommentsEndpoint(fakeAuthenticator, fakeScentry, addCommentUseCase), "/*")
  }

  "POST /commits/:id/comments" should "respond with HTTP 401 when user is not authenticated" in {
    userIsNotAuthenticated
    post("/123/comments") {
      status should be(401)
    }
  }

  "POST /commits/:id/comments" should "create comment for commit" in {
    // given
    val body = "{\"body\": \"This is comment body\"}"
    val dummyComment = Comment(new ObjectId, commitId, user.id, DateTime.now, "This is comment body")
    userIsAuthenticatedAs(user)
    when(addCommentUseCase.execute(any[IncomingComment])).thenReturn(Right(dummyComment))

    // when
    post(s"/$commitId/comments", body, Map("Content-Type" -> "application/json")) {
      // then
      status should be(200)
      val commentArgument = ArgumentCaptor.forClass(classOf[IncomingComment])
      verify(addCommentUseCase).execute(commentArgument.capture())
      commentArgument.getValue.authorId should equal(user.id)
      commentArgument.getValue.commitId should equal(commitId)
      commentArgument.getValue.message should equal("This is comment body")
    }
  }

  "POST /commits/:id/comments" should "create inline comment for commit" in {
    // given
    val body = "{\"body\": \"This is comment body\", \"fileName\": \"test_file.txt\", \"lineNumber\": 20}"
    val dummyComment = Comment(new ObjectId, commitId, user.id, DateTime.now, "This is comment body", Some("test_file.txt"), Some(20))
    userIsAuthenticatedAs(user)
    when(addCommentUseCase.execute(any[IncomingComment])).thenReturn(Right(dummyComment))

    // when
    post(s"/$commitId/comments", body, Map("Content-Type" -> "application/json")) {
      // then
      status should be(200)
      val commentArgument = ArgumentCaptor.forClass(classOf[IncomingComment])
      verify(addCommentUseCase).execute(commentArgument.capture())
      commentArgument.getValue.authorId should equal(user.id)
      commentArgument.getValue.commitId should equal(commitId)
      commentArgument.getValue.message should equal("This is comment body")
      commentArgument.getValue.fileName should equal(dummyComment.fileName)
      commentArgument.getValue.lineNumber should equal(dummyComment.lineNumber)
    }
  }

  "POST /commits/:id/comments" should "return created comment in response" in {
    // given
    val body = "{\"body\": \"This is comment body\"}"
    val createdComment = Comment(new ObjectId, commitId, user.id, DateTime.now, "This is comment body")
    userIsAuthenticatedAs(user)
    when(addCommentUseCase.execute(any[IncomingComment])).thenReturn(Right(createdComment))

    // when
    post(s"/$commitId/comments", body, Map("Content-Type" -> "application/json")) {
      // then
      status should be(200)
      asJson(AddCommentResponse(CommentView(createdComment.id.toString, user.name, user.id.toString, createdComment.message, createdComment.postingTime, user.settings.avatarUrl)))
    }
  }

  def currentUser(id: ObjectId) = User(id, Authentication.basic("user", "password"), "John Doe", "john@doe.com", Set("abcde"))

  class TestableCommentsEndpoint(val authenticator: Authenticator, fakeScentry: Scentry[User], val addCommentUseCase: AddCommentUseCase) extends CommentsEndpoint {
    override def scentry(implicit request: javax.servlet.http.HttpServletRequest) = fakeScentry
  }

}