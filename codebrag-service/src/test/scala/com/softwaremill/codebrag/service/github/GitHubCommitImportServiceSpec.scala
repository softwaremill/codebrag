package com.softwaremill.codebrag.service.github

import org.scalatest.{BeforeAndAfter, FunSpec}
import org.scalatest.mock.MockitoSugar
import org.eclipse.egit.github.core.service.CommitService
import com.softwaremill.codebrag.dao.CommitInfoDAO
import org.mockito._
import org.eclipse.egit.github.core.{RepositoryCommit, IRepositoryIdProvider}
import scala.collection.JavaConversions._
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.BDDMockito._
import org.joda.time.DateTime
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.CommitInfo
import org.bson.types.ObjectId

class GitHubCommitImportServiceSpec extends FunSpec with MockitoSugar with BeforeAndAfter with ShouldMatchers {
  var commitService: CommitService = _
  var converter: GitHubCommitInfoConverter = _
  var dao: CommitInfoDAO = _
  var service: GitHubCommitImportService = _

  before {
    commitService = mock[CommitService]
    converter = mock[GitHubCommitInfoConverter]
    dao = mock[CommitInfoDAO]
    service = new GitHubCommitImportService(commitService, converter, dao)

    given(dao.findAll()) willReturn (List())
  }

  describe("GitHub Commit Service") {
    describe("importing commits for repository") {
      it("should call api for proper repo") {
        //given
        val owner = "a"
        val repo = "b"

        //when
        service.importRepoCommits(owner, repo)

        //then
        verify(commitService).getCommits(argThat(new RepoIdMatcher(owner, repo)))
      }

      it("should convert retrieved commits to internal format") {
        //given
        val commits = List[RepositoryCommit](createRepoCommit("a"), createRepoCommit("b"))
        given(commitService.getCommits(any[IRepositoryIdProvider])).willReturn(commits)

        //when
        service.importRepoCommits("a", "b")

        //then
        verify(converter, times(2)).convertToCommitInfo(any[RepositoryCommit])
      }

      it("should store retrieved commits") {
        //given
        val commits = List[RepositoryCommit](createRepoCommit("a"), createRepoCommit("b"), createRepoCommit("c"))
        given(commitService.getCommits(any[IRepositoryIdProvider])).willReturn(commits)

        //when
        service.importRepoCommits("a", "b")

        //then
        verify(dao, times(3)).storeCommit(any[CommitInfo])
      }

      it("should not access data layer when no commits were retrieved") {
        //given
        given(commitService.getCommits(any[IRepositoryIdProvider])).willReturn(List[RepositoryCommit]())

        //when
        service.importRepoCommits("a", "b")

        //then
        verify(dao, never()).storeCommit(any[CommitInfo])
      }

      it("should store only newest commits") {
        //given
        val date: DateTime = new DateTime
        val oldCommitInfo = CommitInfo(new ObjectId("507f1f77bcf86cd799439011"), "sha", "message", "author", "committer", date, List("parent1"),List.empty)
        val commits = List(oldCommitInfo)
        given(dao.findAll()).willReturn(commits)
        val oldCommit: RepositoryCommit = createRepoCommit("sha")
        val newCommit: RepositoryCommit = createRepoCommit("reposha")
        val retrieved = List(oldCommit, newCommit)
        val newCommitId = new ObjectId("507f1f77bcf86cd799439012");
        given(commitService.getCommits(any[IRepositoryIdProvider])).willReturn(retrieved)
        given(commitService.getCommit(any[IRepositoryIdProvider], Matchers.eq("reposha"))).willReturn(newCommit)
        given(converter.convertToCommitInfo(Matchers.eq(newCommit))).willReturn(CommitInfo(newCommitId, "reposha", "", "", "", new DateTime, List("parent2"), List.empty))
        given(converter.convertToCommitInfo(Matchers.eq(oldCommit))).willReturn(oldCommitInfo)

        //when
        service.importRepoCommits("o", "r")

        //then
        val commitCapturer: ArgumentCaptor[CommitInfo] = ArgumentCaptor.forClass(classOf[CommitInfo])
        verify(dao).storeCommit(commitCapturer.capture())
        val capturedCommits = commitCapturer.getAllValues
        capturedCommits should have size (1)
        capturedCommits.head.sha should be("reposha")
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
