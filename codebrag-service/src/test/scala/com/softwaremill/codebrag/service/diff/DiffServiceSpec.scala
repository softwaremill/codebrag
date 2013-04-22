package com.softwaremill.codebrag.service.diff

import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.dao.{CommitInfoBuilder, CommitInfoDAO}
import com.softwaremill.codebrag.dao.ObjectIdTestUtils._
import org.scalatest.mock.MockitoSugar
import org.mockito.BDDMockito._
import com.softwaremill.codebrag.domain.CommitFileInfo

class DiffServiceSpec extends FlatSpec with BeforeAndAfter with ShouldMatchers with MockitoSugar {

  behavior of "Diff Service for changes in one file"

  val EmptyParentsList = List.empty
  val EmptyFilesList = List.empty
  val FixtureCommitId = oid(1)
  val IrrelevantLineIndicator = -1
  val StatusAdded = "added"
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

  var dao: CommitInfoDAO = _

  before {
    dao = mock[CommitInfoDAO]
    service = new DiffService(dao)
  }

  it should "produce data for each line in diff" in {
    val lines = service.parseDiff(SampleDiff)

    lines should have size (17)
  }

  it should "contain proper number of rows with information headers" in {
    val lines = service.parseDiff(SampleDiff)

    val infoLines = lines.filter(_.line.startsWith("@@"))

    infoLines should have size (2)
  }

  it should "contain proper number of removed lines" in {
    val lines = service.parseDiff(SampleDiff)

    val removedLines = lines.filter(_.line.startsWith("-"))

    removedLines should have size (2)
  }

  it should "container proper number of added lines" in {
    val lines = service.parseDiff(SampleDiff)

    val addedLines = lines.filter(_.line.startsWith("+"))

    addedLines should have size (2)
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
    val commit = CommitInfoBuilder.createRandomCommit()
    given(dao.findByCommitId(FixtureCommitId)).willReturn(Some(commit))

    //when
    val filesEither = service.getFilesWithDiffs(FixtureCommitId.toString)

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
                                                  |                "id":1""")
    val file2 = CommitFileInfo("file2.txt", "", """@@ -47,6 +47,6 @@
                                                  |                 "passsalt":"1231e123123",
                                                  |                 "id":1
                                                  |         },
                                                  |-        "joined":"1900-01-01"
                                                  |+        "joined":"1900-01-02"
                                                  | }
                                                  | ]""")
    val commit = CommitInfoBuilder.createRandomCommitWithFiles(List(file1, file2))
    given(dao.findByCommitId(FixtureCommitId)).willReturn(Some(commit))

    //when
    val Right(files) = service.getFilesWithDiffs(FixtureCommitId.toString)

    //then
    files should have size (2)
    files(0).filename should be("file1.txt")
    files(1).filename should be("file2.txt")
  }

  it should "return information when commit is missing" in {
    //given
    given(dao.findByCommitId(FixtureCommitId)).willReturn(None)

    //when
    val files = service.getFilesWithDiffs(FixtureCommitId.toString)

    //then
    files should be('left)
  }

  it should "return file with no diffs when patch is not available" in {
    //given
    val file = CommitFileInfo("filename.txt", "", null)
    val commit = CommitInfoBuilder.createRandomCommitWithFiles(List(file))
    given(dao.findByCommitId(FixtureCommitId)).willReturn(Some(commit))

    //when
    val Right(files) = service.getFilesWithDiffs(FixtureCommitId.toString)

    //then
    files(0).lines should be ('empty)
  }

  it should "return file with no diffs when patch is empty" in {
    //given
    val file = CommitFileInfo("filename.txt", "", "")
    val commit = CommitInfoBuilder.createRandomCommitWithFiles(List(file))
    given(dao.findByCommitId(FixtureCommitId)).willReturn(Some(commit))

    //when
    val Right(files) = service.getFilesWithDiffs(FixtureCommitId.toString)

    //then
    files(0).lines should be ('empty)
  }

  it should "return status for the file" in {
    //given
    val file = CommitFileInfo("filename.txt", StatusAdded, "")
    val commit = CommitInfoBuilder.createRandomCommitWithFiles(List(file))
    given(dao.findByCommitId(FixtureCommitId)).willReturn(Some(commit))

    //when
    val Right(files) = service.getFilesWithDiffs(FixtureCommitId.toString)

    //then
    files(0).status should be (StatusAdded)
  }
}
