package com.softwaremill.codebrag.service.github.egit

import org.scalatest.{FlatSpec, BeforeAndAfter}
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import org.eclipse.egit.github.core.service.CommitService
import org.mockito.Mockito._
import org.mockito.Matchers._
import scala.collection.JavaConversions._
import org.eclipse.egit.github.core.{CommitUser, Commit, RepositoryCommit}
import com.softwaremill.codebrag.dao.CommitInfoDAO
import org.joda.time.DateTime
import com.softwaremill.codebrag.common.{FakeIdGenerator, IdGenerator}
import org.mockito.stubbing.Answer
import org.mockito.invocation.InvocationOnMock
import com.softwaremill.codebrag.service.github.GitHubCommitInfoConverter

class EgitGithubCommitsLoaderSpec extends FlatSpec with MockitoSugar with BeforeAndAfter with ShouldMatchers {

  implicit val idGenerator: IdGenerator = new FakeIdGenerator("507f1f77bcf86cd799439011")

  var commitService = mock[CommitService]
  var commitInfoDao = mock[CommitInfoDAO]
  val loader = new EgitGitHubCommitsLoader(commitService, commitInfoDao, new GitHubCommitInfoConverter)

  val RepoOwner = "johndoe"
  val RepoName = "project"
  val GithubRepo = GitHubRepositoryIdProvider(RepoOwner, RepoName)
  val EmptyShaSet = Set[String]()

  val GithubCommitsWithSha = githubCommitsWithSha(001, 002, 003, 004, 005)
  val LocalCommits = localCommitsSha(001, 002, 003)
  val MissingLocalCommits = localCommitsSha(004, 005)


  it should "load github commits from correct repo and owner data" in {
    when(commitInfoDao.findAllSha()).thenReturn(EmptyShaSet)

    loader.loadMissingCommits(RepoOwner, RepoName)

    verify(commitService).getCommits(GithubRepo)
  }

  it should "return only commits that don't exist locally" in {
    when(commitService.getCommits(GithubRepo)).thenReturn(GithubCommitsWithSha)
    commitServiceRespondsWithCommitDetails
    when(commitInfoDao.findAllSha()).thenReturn(LocalCommits)

    val commitsLoaded = loader.loadMissingCommits(RepoOwner, RepoName)

    commitsLoaded.map(_.sha).toSet should equal(MissingLocalCommits)
  }

  it should "return no commits when all remote commits are available locally" in {
    when(commitService.getCommits(GithubRepo)).thenReturn(GithubCommitsWithSha)
    when(commitInfoDao.findAllSha()).thenReturn(LocalCommits ++ MissingLocalCommits)

    val commitsLoaded = loader.loadMissingCommits(RepoOwner, RepoName)

    commitsLoaded should have size(0)
  }


  def githubCommitsWithSha(commits: Int*) = {
    commits.map(i => {
      val commit = new RepositoryCommit()
      commit.setSha(s"$i")
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

  def localCommitsSha(commits: Int*) = {
    commits.map{ i => s"$i" }.toSet
  }

  def commitServiceRespondsWithCommitDetails = {
    when(commitService.getCommit(org.mockito.Matchers.eq(GithubRepo), any[String])).thenAnswer(new Answer[RepositoryCommit]() {
      def answer(invocation: InvocationOnMock): RepositoryCommit = {
        val args = invocation.getArguments
        val shaArgument = args(1).asInstanceOf[String]
        githubCommitDetails(shaArgument)
      }
    })
  }

}