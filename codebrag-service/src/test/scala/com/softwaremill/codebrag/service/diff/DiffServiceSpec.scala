package com.softwaremill.codebrag.service.diff

import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatest.matchers.ShouldMatchers

class DiffServiceSpec extends FlatSpec with BeforeAndAfter with ShouldMatchers {
  behavior of "Diff Service for changes in one file"

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

  before {
    service = new DiffService
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

    lines(0).lineNumber should be(0)

    for (i <- 0 to 7) {
      lines(1 + i).lineNumber should be(2 + i)
    }

    lines(9).lineNumber should be(0)

    for {i <- 0 to 6} {
      lines(10 + i).lineNumber should be(47 + i)
    }
  }
}
