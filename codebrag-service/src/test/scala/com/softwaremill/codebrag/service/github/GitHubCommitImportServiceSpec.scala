package com.softwaremill.codebrag.service.github

import org.scalatest.{BeforeAndAfter, GivenWhenThen, FunSpec}
import org.scalatest.mock.MockitoSugar
import org.eclipse.egit.github.core.service.CommitService
import com.softwaremill.codebrag.dao.CommitInfoDAO
import org.mockito._
import org.eclipse.egit.github.core.{RepositoryCommit, IRepositoryIdProvider}
import scala.collection.JavaConversions._
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.joda.time.DateTime
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.CommitInfo

class GitHubCommitImportServiceSpec extends FunSpec with GivenWhenThen with MockitoSugar with BeforeAndAfter with ShouldMatchers {
  var commitService: CommitService = _
  var converter: GitHubCommitInfoConverter = _
  var dao: CommitInfoDAO = _
  var service: GitHubCommitImportService = _

  before {
    commitService = mock[CommitService]
    converter = mock[GitHubCommitInfoConverter]
    dao = mock[CommitInfoDAO]
    service = new GitHubCommitImportService(commitService, converter, dao)

    BDDMockito.given(dao.findAll()) willReturn (List())
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
        verify(commitService).getCommits(argThat(new RepoIdMatcher(owner, repo)))
      }

      it("should convert retrieved commits to internal format") {
        Given("some commits")
        val commits = List[RepositoryCommit](createRepoCommit("a"), createRepoCommit("b"))
        BDDMockito.given(commitService.getCommits(any[IRepositoryIdProvider])).willReturn(commits)

        When("importing them")
        service.importRepoCommits("a", "b")

        Then("they are converted to CommitInfo instances")
        verify(converter, times(2)).convertToCommitInfo(any[RepositoryCommit])
      }

      it("should store retrieved commits") {
        Given("some commits")
        val commits = List[RepositoryCommit](createRepoCommit("a"), createRepoCommit("b"), createRepoCommit("c"))
        BDDMockito.given(commitService.getCommits(any[IRepositoryIdProvider])).willReturn(commits)

        When("importing them")
        service.importRepoCommits("a", "b")

        Then("they should be stored")
        verify(dao).storeCommits(any[List[CommitInfo]])
      }

      it("should not access data layer when no commits were retrieved") {
        Given("no commits")
        BDDMockito.given(commitService.getCommits(any[IRepositoryIdProvider])).willReturn(List[RepositoryCommit]())

        When("running import")
        service.importRepoCommits("a", "b")

        Then("nothing happens after it")
        verify(dao, never()).storeCommit(any[CommitInfo])
      }

      it("should store only newest commits") {
        Given("some stored commits")
        val date: DateTime = new DateTime
        val commits = List(CommitInfo("sha", "message", "author", "committer", date, List("parent1"), List.empty))
        BDDMockito.given(dao.findAll()).willReturn(commits)
        And("some commits in repo")
        val oldCommit: RepositoryCommit = createRepoCommit("sha")
        val newCommit: RepositoryCommit = createRepoCommit("reposha")
        val retrieved = List(oldCommit, newCommit)
        BDDMockito.given(commitService.getCommits(any[IRepositoryIdProvider])).willReturn(retrieved)
        BDDMockito.given(converter.convertToCommitInfo(Matchers.eq(newCommit))).willReturn(CommitInfo("reposha", "", "", "", new DateTime, List("parent2"), List.empty))
        BDDMockito.given(converter.convertToCommitInfo(Matchers.eq(oldCommit))).willReturn(CommitInfo("sha", "message", "author", "committer", date, List("parent1"), List.empty))

        When("importing repo commits")
        service.importRepoCommits("o", "r")

        Then("only commits not already stored are saved")
        val commitCapturer: ArgumentCaptor[Seq[CommitInfo]] = ArgumentCaptor.forClass(classOf[Seq[CommitInfo]])
        verify(dao).storeCommits(commitCapturer.capture())
        val capturedCommits = commitCapturer.getAllValues
        capturedCommits.head should have size (1)
        capturedCommits.head.head.sha should be("reposha")
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
        verify(commitService).getCommit(argThat(new RepoIdMatcher(owner, repo)), Matchers.eq(sha))
      }

      it("should convert commit to internal representation") {
        Given("a commit")
        val commit = createRepoCommit("sha")
        BDDMockito.given(commitService.getCommit(any[IRepositoryIdProvider], Matchers.eq("sha"))).willReturn(commit)

        When("importing it")
        service.importCommitDetails("sha", "o", "r")

        Then("it should be converted")
        verify(converter).convertToCommitInfo(commit)
      }

      it("should store retrieved data") {
        Given("a commit")
        val commit = createRepoCommit("sha")
        BDDMockito.given(commitService.getCommit(any[IRepositoryIdProvider], Matchers.eq("sha"))).willReturn(commit)

        When("importing it")
        service.importCommitDetails("sha", "o", "r")

        Then("it should be stored")
        verify(dao).storeCommit(any[CommitInfo])
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
