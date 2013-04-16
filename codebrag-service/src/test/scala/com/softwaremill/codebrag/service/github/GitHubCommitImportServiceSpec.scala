package com.softwaremill.codebrag.service.github

import org.scalatest.{FlatSpec, BeforeAndAfter}
import org.scalatest.mock.MockitoSugar
import com.softwaremill.codebrag.dao.{CommitInfoBuilder, CommitInfoDAO}
import org.scalatest.matchers.ShouldMatchers
import org.mockito.Mockito._

class GitHubCommitImportServiceSpec extends FlatSpec with MockitoSugar with BeforeAndAfter with ShouldMatchers {

  var commitsLoader: GithubCommitsLoader = _
  var commitInfoDao: CommitInfoDAO = _
  var service: GitHubCommitImportService = _

  val repoOwner = "johndoe"
  val repoName = "project"

  before {
    commitsLoader = mock[GithubCommitsLoader]
    commitInfoDao = mock[CommitInfoDAO]
    service = new GitHubCommitImportService(commitsLoader, commitInfoDao)
  }

  it should "not store anything when no new commits available" in {
    when(commitsLoader.loadMissingCommits(repoOwner, repoName)).thenReturn(List())

    service.importRepoCommits(repoOwner, repoName)

    verifyZeroInteractions(commitInfoDao)
  }

  it should "store all new commits available" in {
    val newCommits = newGithubCommits(5)
    when(commitsLoader.loadMissingCommits(repoOwner, repoName)).thenReturn(newCommits)

    service.importRepoCommits(repoOwner, repoName)

    newCommits.foreach(verify(commitInfoDao).storeCommit(_))
  }

  def newGithubCommits(commitsNumber: Int) = {
    (1 to commitsNumber).map({
      CommitInfoBuilder.createRandomCommit(_)
    }).toList
  }

}