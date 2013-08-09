package com.softwaremill.codebrag.dao.reporting

import com.softwaremill.codebrag.dao._
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.{Authentication, User}
import com.softwaremill.codebrag.dao.reporting.views.{CommitView, CommitListView}
import com.softwaremill.codebrag.test.mongo.ClearDataAfterTest
import com.softwaremill.codebrag.common.LoadMoreCriteria
import org.scalatest.mock.MockitoSugar
import java.util.Date
import org.mockito.Mockito

class MongoCommitWithAuthorDetailsFinderSpec extends FlatSpecWithMongo with ClearDataAfterTest with ShouldMatchers with MockitoSugar {

  val baseCommitFinderMock = mock[MongoCommitFinder]
  val commitWithAuthorFinder = new MongoCommitWithAuthorDetailsFinder(baseCommitFinderMock)

  val userDao = new MongoUserDAO

  val AliceReviewer = ObjectIdTestUtils.oid(22)

  val AvatarForNonexistingUser = ""

  val John = User(ObjectIdTestUtils.oid(123), Authentication.basic("user", "password"), "John Doe", "john@doe.com", "123", "http://john.doe.com/avatar")
  val JohnOnlyEmail = User(ObjectIdTestUtils.oid(124), Authentication.basic("user", "password"), "?", "john@doe.com", "123", "http://johnonlyemail.doe.com/avatar")
  val Alice = User(ObjectIdTestUtils.oid(456), Authentication.basic("user", "password"), "Alice Doe", "alice@doe.com", "456", "http://alice.doe.com/avatar")

  val johnsCommit = CommitView(ObjectIdTestUtils.oid(10).toString, "sha", "this is commit message", "John Doe", "john@doe.com", new Date())
  val bobsCommit = CommitView(ObjectIdTestUtils.oid(20).toString, "sha", "this is commit message", "Bob Nonexisting", "bob@doe.com", new Date())
  val aliceCommit = CommitView(ObjectIdTestUtils.oid(30).toString, "sha", "this is commit message", "Alice Doe", "alice@doe.com", new Date())

  val page = LoadMoreCriteria(None, 10)

  it should "add author avatar to commit" taggedAs(RequiresDb) in {
    testAddAuthorAvatorToCommit(John)
  }

  it should "add author avatar to commit when only the email matches" taggedAs(RequiresDb) in {
    testAddAuthorAvatorToCommit(JohnOnlyEmail)
  }

  private def testAddAuthorAvatorToCommit(johnUser: User) {
    // given
    userDao.add(johnUser)
    Mockito.when(baseCommitFinderMock.findCommitInfoById(johnsCommit.id.toString, AliceReviewer)).thenReturn(Right(johnsCommit))

    // when
    val Right(enrichedCommitView) = commitWithAuthorFinder.findCommitInfoById(johnsCommit.id.toString, AliceReviewer)

    // then
    enrichedCommitView.authorAvatarUrl should equal(johnUser.avatarUrl)
  }

  it should "add empty avatar string to commit if commit author not registered in codebrag" taggedAs(RequiresDb) in {
    // given
    Mockito.when(baseCommitFinderMock.findCommitInfoById(bobsCommit.id.toString, AliceReviewer)).thenReturn(Right(bobsCommit))

    // when
    val Right(enrichedCommitView) = commitWithAuthorFinder.findCommitInfoById(bobsCommit.id.toString, AliceReviewer)

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
    val enrichedCommitsList = commitWithAuthorFinder.findCommitsToReviewForUser(AliceReviewer, page)

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
    val Right(enrichedCommitView) = commitWithAuthorFinder.findCommitInfoById(johnsCommit.id.toString, AliceReviewer)

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
