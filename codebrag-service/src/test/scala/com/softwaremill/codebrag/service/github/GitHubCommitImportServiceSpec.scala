package com.softwaremill.codebrag.service.github

import org.scalatest.{GivenWhenThen, FunSpec}
import org.scalatest.mock.MockitoSugar
import org.eclipse.egit.github.core.service.CommitService
import com.softwaremill.codebrag.dao.CommitInfoDAO
import org.mockito.{Matchers, ArgumentMatcher, Mockito}
import org.eclipse.egit.github.core.IRepositoryIdProvider

class GitHubCommitImportServiceSpec extends FunSpec with GivenWhenThen with MockitoSugar {
  describe("GitHub Commit Service") {
    it("should call api for proper repo") {
      Given("owner and repo")
      val owner = "a"
      val repo = "b"

      val commitService = mock[CommitService]
      val importer = mock[CommitInfoConverter]
      val dao = mock[CommitInfoDAO]

      When("importing from that repo")
      val service = new GitHubCommitImportService(commitService, importer, dao)
      service.importRepoCommits(owner, repo)

      Then("commit service should be called with proper repository id")
      Mockito.verify(commitService).getCommits(Matchers.argThat(new RepoIdMatcher(owner, repo)))
    }

    it("should convert retrieved commits to internal format") {
      pending
    }

    it("should store retrieved commits") {
      pending
    }

    it("should not call dao when no commits were retrieved") {
      pending
    }
  }

}

class RepoIdMatcher(owner: String, repo: String) extends ArgumentMatcher[IRepositoryIdProvider] {
  def matches(argument: Any): Boolean = {
    argument.asInstanceOf[IRepositoryIdProvider].generateId() == s"$owner/$repo"
  }
}
