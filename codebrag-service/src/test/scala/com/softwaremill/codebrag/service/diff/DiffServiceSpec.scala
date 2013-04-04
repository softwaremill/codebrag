package com.softwaremill.codebrag.service.diff

import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.dao.CommitInfoDAO
import org.scalatest.mock.MockitoSugar
import org.mockito.BDDMockito._
import com.softwaremill.codebrag.domain.{CommitInfo, CommitFileInfo}
import org.joda.time.DateTime

class DiffServiceSpec extends FlatSpec with BeforeAndAfter with ShouldMatchers with MockitoSugar {

  behavior of "Diff Service for changes in one file"

  val EmptyParentsList = List.empty
  val EmptyFilesList = List.empty

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

    (lines(0).lineNumberOriginal, lines(0).lineNumberChanged) should be(("0", "0"))
    (lines(1).lineNumberOriginal, lines(1).lineNumberChanged) should be(("2", "2"))
    (lines(2).lineNumberOriginal, lines(2).lineNumberChanged) should be(("3", "3"))
    (lines(3).lineNumberOriginal, lines(3).lineNumberChanged) should be(("4", "4"))
    (lines(4).lineNumberOriginal, lines(4).lineNumberChanged) should be(("5", ""))
    (lines(5).lineNumberOriginal, lines(5).lineNumberChanged) should be(("", "5"))
    (lines(6).lineNumberOriginal, lines(6).lineNumberChanged) should be(("6", "6"))
    (lines(7).lineNumberOriginal, lines(7).lineNumberChanged) should be(("7", "7"))
    (lines(8).lineNumberOriginal, lines(8).lineNumberChanged) should be(("8", "8"))

    (lines(9).lineNumberOriginal, lines(9).lineNumberChanged) should be(("0", "0"))
    (lines(10).lineNumberOriginal, lines(10).lineNumberChanged) should be(("47", "47"))
    (lines(11).lineNumberOriginal, lines(11).lineNumberChanged) should be(("48", "48"))
    (lines(12).lineNumberOriginal, lines(12).lineNumberChanged) should be(("49", "49"))
    (lines(13).lineNumberOriginal, lines(13).lineNumberChanged) should be(("50", ""))
    (lines(14).lineNumberOriginal, lines(14).lineNumberChanged) should be(("", "50"))
    (lines(15).lineNumberOriginal, lines(15).lineNumberChanged) should be(("51", "51"))
    (lines(16).lineNumberOriginal, lines(16).lineNumberChanged) should be(("52", "52"))
  }

  behavior of "Diff service loading files with diffs"

  it should "be right when finds commit" in {
    //given
    val commit = Some(CommitInfo("s", "m", "a", "m", new DateTime, EmptyParentsList, EmptyFilesList))
    given(dao.findBySha("1")).willReturn(commit)

    //when
    val filesEither = service.getFilesWithDiffs("1")

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
    val commit = Some(CommitInfo("sha", "m", "a", "c", new DateTime, EmptyParentsList, List(file1, file2)))
    given(dao.findBySha("1")).willReturn(commit)

    //when
    val Right(files) = service.getFilesWithDiffs("1")

    //then
    files should have size (2)
    files(0).filename should be("file1.txt")
    files(1).filename should be("file2.txt")
  }

  it should "return information when commit is missing" in {
    //given
    given(dao.findBySha("a")).willReturn(None)

    //when
    val files = service.getFilesWithDiffs("a")

    //then
    files should be('left)
  }
}
