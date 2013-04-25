package com.softwaremill.codebrag.service.github

import org.scalatest.{BeforeAndAfter, GivenWhenThen, FunSpec}
import org.eclipse.egit.github.core.{CommitFile, CommitUser, Commit, RepositoryCommit}
import org.scalatest.matchers.ShouldMatchers
import scala.collection.JavaConversions._
import org.joda.time.DateTime
import com.softwaremill.codebrag.common.{FakeIdGenerator, IdGenerator}

class GitHubCommitInfoConverterSpec extends FunSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfter {

  var converter: GitHubCommitInfoConverter = _
  implicit val idGenerator: IdGenerator = new FakeIdGenerator("507f1f77bcf86cd799439011")

  before {
    converter = new GitHubCommitInfoConverter
  }

  describe("GitHub Commit Info Converter") {
    it("should import commit's data") {
      Given("a repository commit")
      val sha = "sha"
      val message: String = "some message"
      val committerName: String = "Soft o'Ware"
      val commitDate = new DateTime().plusMinutes(2)
      val committer = new CommitUser().setName(committerName).setDate(commitDate.toDate)
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
      commitInfo.authorDate should equal(authoredDate)
      commitInfo.commitDate should equal(commitDate)
      commitInfo.parents should equal(parents)
    }


  }

  describe("Commit File Info converter") {
    it("should convert file") {
      Given("a commit file")
      val file = new CommitFile().setFilename("filename.txt").setPatch("patch")

      When("it is converted")
      val commitFileInfo = converter.convertToCommitFileInfo(file)

      Then("it should have proper data")
      commitFileInfo.filename should be("filename.txt")
      commitFileInfo.patch should be("patch")
    }
  }
}
