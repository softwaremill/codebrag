package com.softwaremill.codebrag.dao.reporting

import com.softwaremill.codebrag.dao._
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.{Authentication, User, CommitInfo}
import org.joda.time.DateTime
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.builder.CommitInfoAssembler
import com.softwaremill.codebrag.dao.reporting.views.{CommitView, CommitListView}
import com.softwaremill.codebrag.test.mongo.ClearDataAfterTest
import com.softwaremill.codebrag.common.PagingCriteria
import org.scalatest.mock.MockitoSugar
import java.util.Date
import org.mockito.Mockito


class MongoCommitWithUserDetailsFinderSpec extends FlatSpecWithMongo with ClearDataAfterTest with ShouldMatchers with MockitoSugar {

  val baseCommitFinderMock = mock[MongoCommitFinder]
  val commitWithUserFinder = new MongoCommitWithAuthorDetailsFinder(baseCommitFinderMock)

  val userDao = new MongoUserDAO

  val AliceReviewer = ObjectIdTestUtils.oid(22)

  val AvatarForNonexistingUser = ""

  val John = User(ObjectIdTestUtils.oid(123), Authentication.basic("user", "password"), "John Doe", "john@doe.com", "123", "http://john.doe.com/avatar")
  val Alice = User(ObjectIdTestUtils.oid(456), Authentication.basic("user", "password"), "Alice Doe", "alice@doe.com", "456", "http://alice.doe.com/avatar")

  val johnsCommit = CommitView(ObjectIdTestUtils.oid(10).toString, "sha", "this is commit message", "John Doe", new Date())
  val bobsCommit = CommitView(ObjectIdTestUtils.oid(20).toString, "sha", "this is commit message", "Bob Nonexisting", new Date())
  val aliceCommit = CommitView(ObjectIdTestUtils.oid(30).toString, "sha", "this is commit message", "Alice Doe", new Date())

  val page = PagingCriteria(0, 10)

  it should "add author avatar to commit" taggedAs(RequiresDb) in {
    // given
    userDao.add(John)
    Mockito.when(baseCommitFinderMock.findCommitInfoById(johnsCommit.id.toString, AliceReviewer)).thenReturn(Right(johnsCommit))

    // when
    val Right(enrichedCommitView) = commitWithUserFinder.findCommitInfoById(johnsCommit.id.toString, AliceReviewer)

    // then
    enrichedCommitView.authorAvatarUrl should equal(John.avatarUrl)
  }

  it should "add empty avatar string to commit if commit author not registered in codebrag" taggedAs(RequiresDb) in {
    // given
    Mockito.when(baseCommitFinderMock.findCommitInfoById(bobsCommit.id.toString, AliceReviewer)).thenReturn(Right(bobsCommit))

    // when
    val Right(enrichedCommitView) = commitWithUserFinder.findCommitInfoById(bobsCommit.id.toString, AliceReviewer)

    // then
    enrichedCommitView.authorAvatarUrl should equal(AvatarForNonexistingUser)
  }

  it should "add avatars to all commits in list" taggedAs(RequiresDb) in {
    // given
    userDao.add(John)
    userDao.add(Alice)
    val commits = CommitListView(List(johnsCommit, bobsCommit, aliceCommit), 3)
    Mockito.when(baseCommitFinderMock.findCommitsToReviewForUser(AliceReviewer, page)).thenReturn(commits)

    // when
    val enrichedCommitsList = commitWithUserFinder.findCommitsToReviewForUser(AliceReviewer, page)

    // then
    enrichedCommitsList.commits(0).authorAvatarUrl should equal(John.avatarUrl)
    enrichedCommitsList.commits(1).authorAvatarUrl should equal(AvatarForNonexistingUser)
    enrichedCommitsList.commits(2).authorAvatarUrl should equal(Alice.avatarUrl)
  }

  it should "not corrupt commit data when adding avatars" taggedAs(RequiresDb) in {
    // given
    userDao.add(John)
    Mockito.when(baseCommitFinderMock.findCommitInfoById(johnsCommit.id.toString, AliceReviewer)).thenReturn(Right(johnsCommit))

    // when
    val Right(enrichedCommitView) = commitWithUserFinder.findCommitInfoById(johnsCommit.id.toString, AliceReviewer)

    // then
    enrichedCommitView.authorAvatarUrl should equal(John.avatarUrl)
    assertThatCommitDataAreValid(enrichedCommitView)
  }


  def assertThatCommitDataAreValid(enrichedCommitView: CommitView) {
    enrichedCommitView.id should equal(johnsCommit.id.toString)
    enrichedCommitView.sha should equal(johnsCommit.sha)
    enrichedCommitView.message should equal(johnsCommit.message)
    enrichedCommitView.date should equal(johnsCommit.date)
  }
}
