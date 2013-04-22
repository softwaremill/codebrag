package com.softwaremill.codebrag.service.github.jgit

import org.scalatest.mock.MockitoSugar
import com.softwaremill.codebrag.service.github.{FlatSpecWithGit, CommitReviewTaskGenerator, GitHubCommitImportService}
import com.softwaremill.codebrag.dao.CommitInfoDAO
import org.mockito.{ArgumentCaptor, ArgumentMatcher}
import org.mockito.Mockito._
import com.softwaremill.codebrag.domain.{CommitFileInfo, CommitInfo}
import org.joda.time.DateTime
import org.bson.types.ObjectId
import org.mockito.Matchers._
import scala.collection.JavaConversions._

class JgitGitHubCommitImporterSpec extends FlatSpecWithGit with MockitoSugar {

  var commitInfoDaoMock: CommitInfoDAO = _
  var reviewTaskGeneratorMock: CommitReviewTaskGenerator = _
  var service: GitHubCommitImportService = _
  var supplementaryService: GitHubCommitImportService = _
  val commitInfoDaoSupplementaryStub = mock[CommitInfoDAO]

  before {
    testRepo = initRepo()
    commitInfoDaoMock = mock[CommitInfoDAO]
    reviewTaskGeneratorMock = mock[CommitReviewTaskGenerator]
    service = createService(commitInfoDaoMock)
    supplementaryService = createService(commitInfoDaoSupplementaryStub)
  }

  after {
    deleteRootDirectoryRecursively()
  }

  behavior of "JgitGitHubCommitImporter"

  it should "call persistence to save expected commit data" in {
    // given
    val revCommit = givenCommit("file.txt", "file1 content", "commit1 msg")
    val sha = revCommit.toObjectId.name
    val commitTime = new DateTime(revCommit.getCommitTime * 1000l)
    val expectedPatch = "diff --git a/file.txt b/file.txt\nnew file mode 100644\nindex 0000000..2e80f50\n--- /dev/null\n+++ b/file.txt\n@@ -0,0 +1 @@\n+file1 content\n\\ No newline at end of file\n"
    val expectedCommit = CommitInfo(sha, "commit1 msg", author.getName, committer.getName,
      commitTime, List(), List(CommitFileInfo("file.txt", "added", expectedPatch)))

    // when
    service.importRepoCommits("codebragUser", "remoteRepoName")
    // then
    verify(commitInfoDaoMock).storeCommit(argThat(IsCommitInfoIgnoringId(expectedCommit)))
  }

  it should "call persistence to save empty file" in {
    // given
    val revCommit = givenCommit("file.txt", "", "commit1 msg")
    val sha = revCommit.toObjectId.name
    val commitTime = new DateTime(revCommit.getCommitTime * 1000l)
    val expectedPatch = "diff --git a/file.txt b/file.txt\nnew file mode 100644\nindex 0000000..e69de29\n--- /dev/null\n+++ b/file.txt\n"
    val expectedCommit = CommitInfo(sha, "commit1 msg", author.getName, committer.getName,
      commitTime, List(), List(CommitFileInfo("file.txt", "added", expectedPatch)))

    // when
    service.importRepoCommits("codebragUser", "remoteRepoName")

    // then
    verify(commitInfoDaoMock).storeCommit(argThat(IsCommitInfoIgnoringId(expectedCommit)))
  }

  it should "load only new commits on second call" in {
    // given
    givenCommit("file.txt", "file content", "commit1 msg")
    givenCommit("file.txt", "file content update", "commit2 msg")
    givenAlreadyCalledImport()
    givenCommit("file.txt", "third update content", "third update message")
    givenCommit("file.txt", "fourth update content", "fourth update message")

    // when
    service.importRepoCommits("codebragUser", "remoteRepoName")

    // then
    val commitArgument = ArgumentCaptor.forClass(classOf[CommitInfo])
    verify(commitInfoDaoMock, times(2)).storeCommit(commitArgument.capture())
    val capturedCommits = commitArgument.getAllValues
    capturedCommits(0).message should equal("fourth update message")
    capturedCommits(1).message should equal("third update message")
    verifyNoMoreInteractions(commitInfoDaoMock)
  }

  def givenAlreadyCalledImport() {
    supplementaryService.importRepoCommits("codebragUser", "remoteRepoName")
  }

  def createService(commitInfoDaoMock: CommitInfoDAO) = new GitHubCommitImportService(
    new JgitGitHubCommitsLoader(
      new JgitFacade(credentials),
      new InternalGitDirTree,
      new JgitLogConverter,
      uriBuilder),
    commitInfoDaoMock,
    reviewTaskGeneratorMock)
}

case class IsCommitInfoIgnoringId(otherCommit: CommitInfo) extends ArgumentMatcher[CommitInfo] {
  val constantIrrelevantObjectId = new ObjectId("507f191e810c19729de860e1")

  override def matches(obj: Object): Boolean = {
    val commit = obj.asInstanceOf[CommitInfo]
    commit.copy(id = constantIrrelevantObjectId) equals otherCommit.copy(id = constantIrrelevantObjectId)
  }
}