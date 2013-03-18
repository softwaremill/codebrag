package com.softwaremill.codebrag.service.github

import org.scalatest.{BeforeAndAfter, GivenWhenThen, FunSpec}
import org.eclipse.egit.github.core.{CommitUser, Commit, RepositoryCommit}
import org.scalatest.matchers.ShouldMatchers

class GitHubCommitInfoConverterSpec extends FunSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfter {

  var converter: GitHubCommitInfoConverter = _

  before {
    converter = new GitHubCommitInfoConverter
  }

  describe("GitHub Commit Info Converter") {
    it("should import commit's data") {
      Given("a repository commit")
      val sha = "sha"
      val message: String = "some message"
      val authorName: String = "Soft o'Ware"
      val author = new CommitUser().setName(authorName)
      val rawCommit = new Commit().setMessage(message).setAuthor(author)
      val commit = new RepositoryCommit().setSha(sha).setCommit(rawCommit)

      When("Importer imports that commit")
      val commitInfo = converter.convertToCommitInfo(commit)

      Then("commit info should have proper data")
      commitInfo.sha should equal(sha)
      commitInfo.message should equal(message)
      commitInfo.authorName should equal(authorName)
    }
  }
}
