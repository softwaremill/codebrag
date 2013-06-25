package com.softwaremill.codebrag.service.github.jgit

import org.scalatest.mock.MockitoSugar
import com.softwaremill.codebrag.service.github.{TestCodebragConfig, FlatSpecWithGit, CommitImportService}
import com.softwaremill.codebrag.dao.CommitInfoDAO
import org.mockito.{ArgumentCaptor, ArgumentMatcher}
import org.mockito.Mockito._
import org.mockito.BDDMockito._
import com.softwaremill.codebrag.domain.{CommitFileInfo, CommitInfo}
import org.joda.time.DateTime
import org.bson.types.ObjectId
import org.mockito.Matchers._
import scala.collection.JavaConversions._
import com.softwaremill.codebrag.service.events.MockEventBus

class JgitGitHubCommitImporterSpec extends FlatSpecWithGit with MockitoSugar with MockEventBus {

  var commitInfoDaoMock: CommitInfoDAO = _
  var service: CommitImportService = _
  var supplementaryService: CommitImportService = _
  val commitInfoDaoSupplementaryStub = mock[CommitInfoDAO]

  before {
    eventBus.clear()
    testRepo = initRepo()
    commitInfoDaoMock = mock[CommitInfoDAO]
    service = createService(commitInfoDaoMock)
    supplementaryService = createService(commitInfoDaoSupplementaryStub)
  }

  after {
    deleteRootDirectoryRecursively()
  }

  behavior of "JgitGitHubCommitImporter"

  it should "call persistence to save expected commit data" in {
    // given
    val parentId = givenInitialCommit()
    val revCommit = givenCommit("file.txt", "file1 content", "commit1 msg")
    val sha = revCommit.toObjectId.name
    val commitTime = new DateTime(revCommit.getCommitTime * 1000l)
    val authorTime = new DateTime(revCommit.getAuthorIdent.getWhen)
    val expectedPatch = "diff --git a/file.txt b/file.txt\nnew file mode 100644\nindex 0000000..2e80f50\n--- /dev/null\n+++ b/file.txt\n@@ -0,0 +1 @@\n+file1 content\n\\ No newline at end of file\n"

    val expectedCommit = CommitInfo(sha, "commit1 msg", author.getName, committer.getName,
      commitTime, authorTime, List(parentId), List(CommitFileInfo("file.txt", "added", expectedPatch)))

    // when
    service.importRepoCommits(TestRepoData)
    // then
    verify(commitInfoDaoMock).storeCommit(argThat(IsCommitInfoIgnoringId(expectedCommit)))
  }

  it should "call persistence to save empty file" in {
    // given
    val parentId = givenInitialCommit()
    val revCommit = givenCommit("file.txt", "", "commit1 msg")
    val sha = revCommit.toObjectId.name
    val commitTime = new DateTime(revCommit.getCommitTime * 1000l)
    val authorTime = new DateTime(revCommit.getAuthorIdent.getWhen)
    val expectedPatch = "diff --git a/file.txt b/file.txt\nnew file mode 100644\nindex 0000000..e69de29\n--- /dev/null\n+++ b/file.txt\n"
    val expectedCommit = CommitInfo(sha, "commit1 msg", author.getName, committer.getName,
      commitTime, authorTime, List(parentId), List(CommitFileInfo("file.txt", "added", expectedPatch)))

    // when
    service.importRepoCommits(TestRepoData)

    // then
    verify(commitInfoDaoMock).storeCommit(argThat(IsCommitInfoIgnoringId(expectedCommit)))
  }

  it should "load only new commits on second call" in {
    // given
    givenInitialCommit()
    val lastCommit = givenCommit("file.txt", "file content update", "commit2 msg")
    givenAlreadyCalledImport()
    given(commitInfoDaoMock.findLastSha()).willReturn(Some(lastCommit.getId.name))
    givenCommit("file.txt", "third update content", "third update message")
    givenCommit("file.txt", "fourth update content", "fourth update message")

    // when
    service.importRepoCommits(TestRepoData)

    // then
    val commitArgument = ArgumentCaptor.forClass(classOf[CommitInfo])
    verify(commitInfoDaoMock).hasCommits
    verify(commitInfoDaoMock).findLastSha()
    verify(commitInfoDaoMock, times(2)).storeCommit(commitArgument.capture())
    val capturedCommits = commitArgument.getAllValues
    capturedCommits(0).message should equal("fourth update message")
    capturedCommits(1).message should equal("third update message")
    verifyNoMoreInteractions(commitInfoDaoMock)
  }

  private def givenInitialCommit() = givenCommit("some-file", "content", "Initial commit").getId.name()

  private def givenAlreadyCalledImport() {
    supplementaryService.importRepoCommits(TestRepoData)
  }

  private def createService(commitInfoDaoMock: CommitInfoDAO) = new CommitImportService(
    new JgitCommitsLoader(
      new JgitFacade(credentials),
      new InternalGitDirTree(TestCodebragConfig),
      new JgitLogConverter,
      commitInfoDaoMock),
    commitInfoDaoMock,
    eventBus)
}

case class IsCommitInfoIgnoringId(otherCommit: CommitInfo) extends ArgumentMatcher[CommitInfo] {
  val constantIrrelevantObjectId = new ObjectId("507f191e810c19729de860e1")

  override def matches(obj: Object): Boolean = {
    val commit = obj.asInstanceOf[CommitInfo]
    commit.copy(id = constantIrrelevantObjectId) equals otherCommit.copy(id = constantIrrelevantObjectId)
  }
}