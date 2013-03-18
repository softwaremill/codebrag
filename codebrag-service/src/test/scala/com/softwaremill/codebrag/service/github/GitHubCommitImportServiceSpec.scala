package com.softwaremill.codebrag.service.github

import org.scalatest.{BeforeAndAfter, GivenWhenThen, FunSpec}
import org.scalatest.mock.MockitoSugar
import org.eclipse.egit.github.core.service.CommitService
import com.softwaremill.codebrag.dao.CommitInfoDAO
import org.mockito.{Matchers, ArgumentMatcher, Mockito}
import org.eclipse.egit.github.core.{RepositoryCommit, IRepositoryIdProvider}
import com.softwaremill.codebrag.domain.CommitInfo
import scala.collection.JavaConversions._

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
    it("should call api for proper repo") {
      Given("owner and repo")
      val owner = "a"
      val repo = "b"

      When("importing from that repo")
      service.importRepoCommits(owner, repo)

      Then("commit service should be called with proper repository id")
      Mockito.verify(commitService).getCommits(Matchers.argThat(new RepoIdMatcher(owner, repo)))
    }

    it("should convert retrieved commits to internal format") {
      Given("some commits")
      val commits = List[RepositoryCommit](createRepoCommit("a"), createRepoCommit("b"))
      Mockito.when(commitService.getCommits(Matchers.any[IRepositoryIdProvider])).thenReturn(commits)

      When("importing them")
      service.importRepoCommits("a", "b")

      Then("they are converted to CommitInfo instances")
      Mockito.verify(converter, Mockito.times(2)).convertToCommitInfo(Matchers.any[RepositoryCommit])
    }

    it("should store retrieved commits") {
      Given("some commits")
      val commits = List[RepositoryCommit](createRepoCommit("a"), createRepoCommit("b"), createRepoCommit("c"))
      Mockito.when(commitService.getCommits(Matchers.any[IRepositoryIdProvider])).thenReturn(commits)

      When("importing them")
      service.importRepoCommits("a", "b")

      Then("they should be stored")
      Mockito.verify(dao, Mockito.times(1)).storeCommits(Matchers.any[List[CommitInfo]])
    }

    it("should not access data layer when no commits were retrieved") {
      Given("no commits")
      Mockito.when(commitService.getCommits(Matchers.any[IRepositoryIdProvider])).thenReturn(List[RepositoryCommit]())

      When("running import")
      service.importRepoCommits("a", "b")

      Then("nothing happens after it")
      Mockito.verify(dao, Mockito.never()).storeCommit(Matchers.any[CommitInfo])
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
