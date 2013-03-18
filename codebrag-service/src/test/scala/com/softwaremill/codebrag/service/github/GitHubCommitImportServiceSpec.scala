package com.softwaremill.codebrag.service.github

import org.scalatest.{BeforeAndAfter, GivenWhenThen, FunSpec}
import org.scalatest.mock.MockitoSugar
import org.eclipse.egit.github.core.service.CommitService
import com.softwaremill.codebrag.dao.CommitInfoDAO
import org.mockito.{Matchers, ArgumentMatcher, Mockito}
import org.eclipse.egit.github.core.{RepositoryCommit, IRepositoryIdProvider}
import com.softwaremill.codebrag.domain.CommitInfo
import scala.collection.JavaConversions._
import org.mockito.Matchers._

class GitHubCommitImportServiceSpec extends FunSpec with GivenWhenThen with MockitoSugar with BeforeAndAfter {
  var commitService: CommitService = _
  var converter: GitHubCommitInfoConverter = _
  var dao: CommitInfoDAO = _
  var service: GitHubCommitImportService = _

  before {
    commitService = mock[CommitService]
    converter = mock[GitHubCommitInfoConverter]
    dao = mock[CommitInfoDAO]
    service = new GitHubCommitImportService(commitService, converter, dao)
  }

  describe("GitHub Commit Service") {
    describe("importing commits for repository") {
      it("should call api for proper repo") {
        Given("owner and repo")
        val owner = "a"
        val repo = "b"

        When("importing from that repo")
        service.importRepoCommits(owner, repo)

        Then("commit service should be called with proper repository id")
        Mockito.verify(commitService).getCommits(argThat(new RepoIdMatcher(owner, repo)))
      }

      it("should convert retrieved commits to internal format") {
        Given("some commits")
        val commits = List[RepositoryCommit](createRepoCommit("a"), createRepoCommit("b"))
        Mockito.when(commitService.getCommits(any[IRepositoryIdProvider])).thenReturn(commits)

        When("importing them")
        service.importRepoCommits("a", "b")

        Then("they are converted to CommitInfo instances")
        Mockito.verify(converter, Mockito.times(2)).convertToCommitInfo(any[RepositoryCommit])
      }

      it("should store retrieved commits") {
        Given("some commits")
        val commits = List[RepositoryCommit](createRepoCommit("a"), createRepoCommit("b"), createRepoCommit("c"))
        Mockito.when(commitService.getCommits(any[IRepositoryIdProvider])).thenReturn(commits)

        When("importing them")
        service.importRepoCommits("a", "b")

        Then("they should be stored")
        Mockito.verify(dao, Mockito.times(1)).storeCommits(any[List[CommitInfo]])
      }

      it("should not access data layer when no commits were retrieved") {
        Given("no commits")
        Mockito.when(commitService.getCommits(any[IRepositoryIdProvider])).thenReturn(List[RepositoryCommit]())

        When("running import")
        service.importRepoCommits("a", "b")

        Then("nothing happens after it")
        Mockito.verify(dao, Mockito.never()).storeCommit(any[CommitInfo])
      }
    }

    describe("importing a single commit") {
      it("should call api for proper repo") {
        Given("some commit id")
        And("owner")
        And("repo")
        val owner = "a"
        val repo = "b"
        val sha = "somesha"

        When("importing from that repo")
        service.importCommitDetails(sha, owner, repo)

        Then("commit service should be called with proper repository id")
        Mockito.verify(commitService).getCommit(argThat(new RepoIdMatcher(owner, repo)), Matchers.eq(sha))
      }

      it("should convert commit to internal representation") {
        Given("a commit")
        val commit = createRepoCommit("sha")
        Mockito.when(commitService.getCommit(any[IRepositoryIdProvider], Matchers.eq("sha"))).thenReturn(commit)

        When("importing it")
        service.importCommitDetails("sha", "o", "r")

        Then("it should be converted")
        Mockito.verify(converter, Mockito.times(1)).convertToCommitInfo(commit)
      }

      it("should store retrieved data") {
        Given("a commit")
        val commit = createRepoCommit("sha")
        Mockito.when(commitService.getCommit(any[IRepositoryIdProvider], Matchers.eq("sha"))).thenReturn(commit)

        When("importing it")
        service.importCommitDetails("sha", "o", "r")

        Then("it should be stored")
        Mockito.verify(dao, Mockito.times(1)).storeCommit(any[CommitInfo])
      }
    }
  }

  private def createRepoCommit(sha: String): RepositoryCommit = {
    val commit = new RepositoryCommit
    commit.setSha(sha)
    commit
  }
}

class RepoIdMatcher(owner: String, repo: String) extends ArgumentMatcher[IRepositoryIdProvider] {
  def matches(argument: Any): Boolean = {
    argument.asInstanceOf[IRepositoryIdProvider].generateId() == s"$owner/$repo"
  }
}
