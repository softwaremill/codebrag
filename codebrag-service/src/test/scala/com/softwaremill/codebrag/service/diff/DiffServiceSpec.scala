package com.softwaremill.codebrag.service.diff

import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.mockito.BDDMockito._
import com.softwaremill.codebrag.domain.CommitFileInfo
import com.softwaremill.codebrag.domain.builder.CommitInfoAssembler
import com.softwaremill.codebrag.service.commits.DiffLoader
import com.softwaremill.codebrag.repository.Repository

class DiffServiceSpec extends FlatSpec with BeforeAndAfter with ShouldMatchers with MockitoSugar {

  behavior of "Diff Service for changes in one file"

  val EmptyParentsList = List.empty
  val EmptyFilesList = List.empty
  val IrrelevantLineIndicator = -1
  val StatusAdded = "added"
  val FileWithPatchHeaders = CommitFileInfo("file3.txt", "",
    """diff --git a/codebrag-ui/src/main/webapp/index.html b/codebrag-ui/src/main/webapp/index.html
      |index 0f62727..e08a092 100644
      |--- a/codebrag-ui/src/main/webapp/index.html
      |+++ b/codebrag-ui/src/main/webapp/index.html
      |@@ -47,6 +47,6 @@
      |some content
      |@@ -147,6 +147,6 @@
      |another content
      |@@ -247,6 +247,6 @@
      |yet another content""".stripMargin)

  val SampleDiff =
    """@@ -2,7 +2,7 @@
      | {
      |        "user":
      |        {
      |-               "login":"foo",
      |+               "login":"foobar",
      |                "pass":"2hf23jbd23d2839f2kejdn",
      |                "passsalt":"1231e123123",
      |                "id":1
      |@@ -47,6 +47,6 @@
      |                 "passsalt":"1231e123123",
      |                 "id":1
      |         },
      |-        "joined":"1900-01-01"
      |+        "joined":"1900-01-02"
      | }
      | ]""".stripMargin

  var service: DiffService = _

  var repository: Repository = _

  var diffLoader: DiffLoader = _

  before {
    diffLoader = mock[DiffLoader]
    repository = mock[Repository]
    service = new DiffService(diffLoader, repository)
  }

  it should "produce data for each line in diff" in {
    val lines = service.parseDiff(SampleDiff)

    lines should have size 17
  }

  it should "contain proper number of rows with information headers" in {
    val lines = service.parseDiff(SampleDiff)

    val infoLines = lines.filter(_.line.startsWith("@@"))

    infoLines should have size 2
  }

  it should "contain proper number of removed lines" in {
    val lines = service.parseDiff(SampleDiff)

    val removedLines = lines.filter(_.line.startsWith("-"))

    removedLines should have size 2
  }

  it should "container proper number of added lines" in {
    val lines = service.parseDiff(SampleDiff)

    val addedLines = lines.filter(_.line.startsWith("+"))

    addedLines should have size 2
  }

  it should "assign proper line numbers to diff lines" in {
    val lines = service.parseDiff(SampleDiff)
    def line(number: Int) = (lines(number).lineNumberOriginal, lines(number).lineNumberChanged)

    line(0) should be((IrrelevantLineIndicator, IrrelevantLineIndicator))
    line(1) should be((2, 2))
    line(2) should be((3, 3))
    line(3) should be((4, 4))
    line(4) should be((5, IrrelevantLineIndicator))
    line(5) should be((IrrelevantLineIndicator, 5))
    line(6) should be((6, 6))
    line(7) should be((7, 7))
    line(8) should be((8, 8))

    line(9) should be((IrrelevantLineIndicator, IrrelevantLineIndicator))
    line(10) should be((47, 47))
    line(11) should be((48, 48))
    line(12) should be((49, 49))
    line(13) should be((50, IrrelevantLineIndicator))
    line(14) should be((IrrelevantLineIndicator, 50))
    line(15) should be((51, 51))
    line(16) should be((52, 52))
  }

  behavior of "Diff service loading files with diffs"

  it should "be right when finds commit" in {
    //given
    val commit = CommitInfoAssembler.randomCommit.get
    given(diffLoader.loadDiff(commit.sha, repository)).willReturn(Some(Nil))

    //when
    val filesEither = service.getFilesWithDiffs(commit.sha)

    //then
    filesEither should be('right)
  }

  it should "return list of files with their diffs" in {
    //given
    val file1 = CommitFileInfo("file1.txt", "", """@@ -2,7 +2,7 @@
                                                  | {
                                                  |        "user":
                                                  |        {
                                                  |-               "login":"foo",
                                                  |+               "login":"foobar",
                                                  |                "pass":"2hf23jbd23d2839f2kejdn",
                                                  |                "passsalt":"1231e123123",
                                                  |                "id":1""".stripMargin)
    val file2 = CommitFileInfo("file2.txt", "", """@@ -47,6 +47,6 @@
                                                  |                 "passsalt":"1231e123123",
                                                  |                 "id":1
                                                  |         },
                                                  |-        "joined":"1900-01-01"
                                                  |+        "joined":"1900-01-02"
                                                  | }
                                                  | ]""".stripMargin)
    val commit = CommitInfoAssembler.randomCommit.get
    given(diffLoader.loadDiff(commit.sha, repository)).willReturn(Some(List(file1, file2)))

    //when
    val Right(files) = service.getFilesWithDiffs(commit.sha)

    //then
    files should have size 2
    files(0).filename should be("file1.txt")
    files(0).lines(0).line should equal("@@ -2,7 +2,7 @@")
    files(0).lines(2).line should equal("        \"user\":")
    files(1).filename should be("file2.txt")
  }

  it should "contain diff stats for file" in {
    val file = CommitFileInfo("file1.txt", "", """@@ -2,7 +2,7 @@
                                                 |-               "login":"foo",
                                                 |+               "login":"foobar",
                                                 |+               "pass": "123",
                                                 |+               "age": 32,
                                                 |                "id":1""".stripMargin)
    val commit = CommitInfoAssembler.randomCommit.get
    given(diffLoader.loadDiff(commit.sha, repository)).willReturn(Some(List(file)))

    //when
    val Right(files) = service.getFilesWithDiffs(commit.sha)

    //then
    files(0).diffStats.added should equal(3)
    files(0).diffStats.removed should equal(1)
  }

  it should "cut git headers" in {
    // given
    val commit = CommitInfoAssembler.randomCommit.get
    given(diffLoader.loadDiff(commit.sha, repository)).willReturn(Some(List(FileWithPatchHeaders)))

    // when
    val Right(files) = service.getFilesWithDiffs(commit.sha)

    // then
    files(0).lines.length should equal(6)
    files(0).lines(0).line should equal("@@ -47,6 +47,6 @@")
    files(0).lines(1).line should equal("some content")
    files(0).lines(2).line should equal("@@ -147,6 +147,6 @@")
    files(0).lines(3).line should equal("another content")
    files(0).lines(4).line should equal("@@ -247,6 +247,6 @@")
    files(0).lines(5).line should equal("yet another content")
  }

  it should "return information when commit is missing" in {
    //given
    val nonExistingSHA = "123123123"
    given(diffLoader.loadDiff(nonExistingSHA, repository)).willReturn(None)

    //when
    val files = service.getFilesWithDiffs(nonExistingSHA)

    //then
    files should be('left)
  }

  it should "return file with no diffs when patch is not available" in {
    //given
    val file = CommitFileInfo("filename.txt", "", null)
    val commit = CommitInfoAssembler.randomCommit.get
    given(diffLoader.loadDiff(commit.sha, repository)).willReturn(Some(List(file)))

    //when
    val Right(files) = service.getFilesWithDiffs(commit.sha)

    //then
    files(0).lines should be ('empty)
  }

  it should "return file with no diffs when patch is empty" in {
    //given
    val file = CommitFileInfo("filename.txt", "", "")
    val commit = CommitInfoAssembler.randomCommit.get
    given(diffLoader.loadDiff(commit.sha, repository)).willReturn(Some(List(file)))

    //when
    val Right(files) = service.getFilesWithDiffs(commit.sha)

    //then
    files(0).lines should be ('empty)
  }

  it should "return status for the file" in {
    //given
    val file = CommitFileInfo("filename.txt", StatusAdded, "")
    val commit = CommitInfoAssembler.randomCommit.get
    given(diffLoader.loadDiff(commit.sha, repository)).willReturn(Some(List(file)))

    //when
    val Right(files) = service.getFilesWithDiffs(commit.sha)

    //then
    files(0).status should be (StatusAdded)
  }
}
