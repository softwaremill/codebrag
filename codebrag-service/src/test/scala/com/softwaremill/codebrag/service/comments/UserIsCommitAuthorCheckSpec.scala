package com.softwaremill.codebrag.service.comments

import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.dao.{CommitInfoDAO}
import org.mockito.Mockito._
import com.softwaremill.codebrag.domain.Like
import com.softwaremill.codebrag.domain.builder.{UserAssembler, CommitInfoAssembler}
import org.bson.types.ObjectId
import org.joda.time.DateTime
import com.softwaremill.codebrag.dao.user.UserDAO

class UserIsCommitAuthorCheckSpec extends FlatSpec with MockitoSugar with ShouldMatchers with BeforeAndAfterEach {

  var commitDaoMock: CommitInfoDAO = _
  var userDaoMock: UserDAO = _

  var userIsCommitAuthorCheck: UserIsCommitAuthorCheck = _

  val AuthorName = "John Doe"
  val CommitId = new ObjectId
  val Commit = CommitInfoAssembler.randomCommit.withId(CommitId).withAuthorName(AuthorName).get

  val LikeUser = UserAssembler.randomUser.get//User(new ObjectId, Authentication.basic("john", "password"), "John Doe", "john@doe.com", "123", "")
  val AnotherUser = UserAssembler.randomUser.get//User(new ObjectId, Authentication.basic("mary", "password"), "Mary Smith", "mary@smith.com", "123", "")

  val LikeToValidate = Like(new ObjectId, CommitId, LikeUser.id, DateTime.now, Some("file.txt"), Some(20))


  override def beforeEach() {

    commitDaoMock = mock[CommitInfoDAO]
    userDaoMock = mock[UserDAO]

    userIsCommitAuthorCheck = new UserIsCommitAuthorCheck {
      def userDao: UserDAO = userDaoMock
      def commitDao: CommitInfoDAO = commitDaoMock
    }
  }

  it should "return true when like user is also commit author" in {
    // given
    when(commitDaoMock.findByCommitId(CommitId)).thenReturn(Some(Commit))
    when(userDaoMock.findCommitAuthor(Commit)).thenReturn(Some(LikeUser))

    // when
    val result = userIsCommitAuthorCheck.userIsCommitAuthor(LikeToValidate)

    // then
    result should be(true)
  }

  it should "return false when like user is not commit author" in {
    // given
    when(commitDaoMock.findByCommitId(CommitId)).thenReturn(Some(Commit))
    when(userDaoMock.findCommitAuthor(Commit)).thenReturn(Some(AnotherUser))

    // when
    val result = userIsCommitAuthorCheck.userIsCommitAuthor(LikeToValidate)

    // then
    result should be(false)
  }

  it should "return false when commit author cannot be found" in {
    // given
    when(commitDaoMock.findByCommitId(CommitId)).thenReturn(Some(Commit))
    when(userDaoMock.findCommitAuthor(Commit)).thenReturn(None)

    // when
    val result = userIsCommitAuthorCheck.userIsCommitAuthor(LikeToValidate)

    // then
    result should be(false)
  }

}
