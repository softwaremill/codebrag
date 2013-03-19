package com.softwaremill.codebrag.service.github

import org.scalatest.{BeforeAndAfter, GivenWhenThen, FunSpec}
import org.eclipse.egit.github.core.{CommitUser, Commit, RepositoryCommit}
import org.scalatest.matchers.ShouldMatchers
import java.util.Date
import scala.collection.JavaConversions._
import org.joda.time.DateTime

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
      val committerName: String = "Soft o'Ware"
      val committer = new CommitUser().setName(committerName)
      val authorName: String = "Software Millkovsky"
      val authoredDate = new DateTime()
      val author = new CommitUser().setName(authorName).setDate(authoredDate.toDate)
      val parents = List("a", "b")
      val parentCommits = List(new Commit().setSha("a"), new Commit().setSha("b"))
      val rawCommit = new Commit().setMessage(message).setCommitter(committer).setAuthor(author)
      val commit = new RepositoryCommit().setSha(sha).setCommit(rawCommit).setParents(parentCommits)

      When("Importer imports that commit")
      val commitInfo = converter.convertToCommitInfo(commit)

      Then("commit info should have proper data")
      commitInfo.sha should equal(sha)
      commitInfo.message should equal(message)
      commitInfo.authorName should equal(authorName)
      commitInfo.committerName should equal(committerName)
      commitInfo.date should equal(authoredDate)
      commitInfo.parents should equal(parents)
    }
  }
}
