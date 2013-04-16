package com.softwaremill.codebrag.service.github

import org.scalatest.{FlatSpec, BeforeAndAfter}
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import org.eclipse.egit.github.core.service.CommitService
import org.mockito.Mockito._
import org.mockito.Matchers._
import scala.collection.JavaConversions._
import org.eclipse.egit.github.core.{CommitUser, Commit, RepositoryCommit}
import com.softwaremill.codebrag.dao.CommitInfoDAO
import com.softwaremill.codebrag.domain.CommitInfo
import org.joda.time.DateTime
import com.softwaremill.codebrag.common.{FakeIdGenerator, IdGenerator}
import org.mockito.stubbing.Answer
import org.mockito.invocation.InvocationOnMock

class GithubCommitsLoaderSpec extends FlatSpec with MockitoSugar with BeforeAndAfter with ShouldMatchers {

  implicit val idGenerator: IdGenerator = new FakeIdGenerator("507f1f77bcf86cd799439011")

  var commitService = mock[CommitService]
  var commitInfoDao = mock[CommitInfoDAO]
  val loader = new GithubCommitsLoader(commitService, commitInfoDao, new GitHubCommitInfoConverter)

  val RepoOwner = "johndoe"
  val RepoName = "project"
  val GithubRepo = GithubRepositoryIdProvider(RepoOwner, RepoName)
  val EmptyList = List()

  val GithubCommitsList = githubCommitsList(1, 2, 3, 4, 5)
  val LocalCommits = localCommits(1, 2, 3)
  val MissingLocalCommits = localCommits(4, 5)


  it should "load github commits from correct repo and owner data" in {
    when(commitInfoDao.findAll()).thenReturn(EmptyList)

    loader.loadMissingCommits(RepoOwner, RepoName)

    verify(commitService).getCommits(GithubRepo)
  }

  it should "return only commits that don't exist locally" in {
    when(commitService.getCommits(GithubRepo)).thenReturn(GithubCommitsList)
    commitServiceRespondsWithCommitDetails
    when(commitInfoDao.findAll()).thenReturn(LocalCommits)

    val commitsLoaded = loader.loadMissingCommits(RepoOwner, RepoName)

    commitsLoaded.map(_.sha) should equal(MissingLocalCommits.map(_.sha))
  }

  it should "return no commits when all remote commits are available locally" in {
    when(commitService.getCommits(GithubRepo)).thenReturn(GithubCommitsList)
    when(commitInfoDao.findAll()).thenReturn(LocalCommits ::: MissingLocalCommits)

    val commitsLoaded = loader.loadMissingCommits(RepoOwner, RepoName)

    commitsLoaded should have size(0)
  }


  def githubCommitsList(commits: Int*) = {
    commits.map(i => {
      val commit = new RepositoryCommit()
      commit.setSha(s"000${i}")
      commit
    }).toList
  }

  def githubCommitDetails(sha: String) = {
    val repoCommit = new RepositoryCommit()
    val commitData = new Commit()
    val commitAuthor = new CommitUser()

    repoCommit.setSha(sha)

    commitAuthor.setName("Jonh Doe")
    commitAuthor.setDate(DateTime.now().toDate)

    repoCommit.setParents(List())
    repoCommit.setFiles(List())

    repoCommit.setCommit(commitData)
    commitData.setAuthor(commitAuthor)
    commitData.setCommitter(commitAuthor)

    repoCommit
  }

  def localCommits(commits: Int*) = {
    commits.map(i => {
      CommitInfo(s"000${i}", null, null, null, null, null, null)
    }).toList
  }

  def commitServiceRespondsWithCommitDetails {
    when(commitService.getCommit(org.mockito.Matchers.eq(GithubRepo), any[String])).thenAnswer(new Answer[RepositoryCommit]() {
      def answer(invocation: InvocationOnMock): RepositoryCommit = {
        val args = invocation.getArguments
        val shaArgument = args(1).asInstanceOf[String]
        githubCommitDetails(shaArgument)
      }
    })
  }

}