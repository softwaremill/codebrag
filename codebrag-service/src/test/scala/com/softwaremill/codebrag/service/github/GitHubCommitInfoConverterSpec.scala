package com.softwaremill.codebrag.service.github

import org.scalatest.{GivenWhenThen, FunSpec}
import org.eclipse.egit.github.core.RepositoryCommit
import com.softwaremill.codebrag.common.Utils
import org.scalatest.matchers.ShouldMatchers

class GitHubCommitInfoConverterSpec extends FunSpec with GivenWhenThen with ShouldMatchers {
  describe("GitHub Commit Info Converter") {
    it("should import sha of commit") {
      val sha: String = Utils.sha1("a")
      Given(s"repository commit with sha $sha")
      val commit = new RepositoryCommit
      commit.setSha(sha)

      When("Importer imports that commit")
      val importer = new GitHubCommitInfoConverter
      val commitInfo = importer.convertToCommitInfo(commit)

      Then("commit info should have the same sha")
      commitInfo.sha should equal(sha)
    }
  }
}
