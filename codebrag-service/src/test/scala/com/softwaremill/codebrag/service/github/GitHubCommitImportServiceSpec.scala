package com.softwaremill.codebrag.service.github

import org.scalatest.{FlatSpec, BeforeAndAfter}
import org.scalatest.mock.MockitoSugar
import com.softwaremill.codebrag.dao.CommitInfoDAO
import org.scalatest.matchers.ShouldMatchers
import org.mockito.Mockito._
import com.softwaremill.codebrag.domain.CommitInfo
import com.softwaremill.codebrag.domain.builder.CommitInfoAssembler
import org.bson.types.ObjectId

class GitHubCommitImportServiceSpec extends FlatSpec with MockitoSugar with BeforeAndAfter with ShouldMatchers {

  var commitsLoader: GitHubCommitsLoader = _
  var commitInfoDao: CommitInfoDAO = _
  var reviewTaskGenerator: CommitReviewTaskGenerator = _
  var service: GitHubCommitImportService = _

  val repoOwner = "johndoe"
  val repoName = "project"

  val EmptyCommitsList = List[CommitInfo]()

  before {
    commitsLoader = mock[GitHubCommitsLoader]
    commitInfoDao = mock[CommitInfoDAO]
    reviewTaskGenerator = mock[CommitReviewTaskGenerator]
    service = new GitHubCommitImportService(commitsLoader, commitInfoDao, reviewTaskGenerator)
  }

  it should "not store anything when no new commits available" in {
    when(commitsLoader.loadMissingCommits(repoOwner, repoName)).thenReturn(EmptyCommitsList)

    service.importRepoCommits(repoOwner, repoName)

    verifyZeroInteractions(commitInfoDao)
    verifyZeroInteractions(reviewTaskGenerator)
  }

  it should "store all new commits available" in {
    val newCommits = newGithubCommits(5)
    when(commitsLoader.loadMissingCommits(repoOwner, repoName)).thenReturn(newCommits)

    service.importRepoCommits(repoOwner, repoName)

    newCommits.foreach(commit => {
      verify(commitInfoDao).storeCommit(commit)
      verify(reviewTaskGenerator).createReviewTasksFor(commit)
    })
  }

  def newGithubCommits(commitsNumber: Int) = {
    (1 to commitsNumber).map( num => {
      CommitInfoAssembler.randomCommit.withId(ObjectId.massageToObjectId(num)).get
    }).toList
  }

}