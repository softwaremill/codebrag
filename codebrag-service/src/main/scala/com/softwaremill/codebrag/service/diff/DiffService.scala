package com.softwaremill.codebrag.service.diff

class DiffService {

  val Info = """@@ -(\d+),(\d+) \+(\d+),(\d+) @@""".r

  def parseDiff(diff: String): List[DiffLine] = {
    def convertToDiffLines(lines: List[String], lineNumber: Int): List[DiffLine] = {
      if (lines.isEmpty) {
        List()
      } else {
        lines.head match {
          case line@Info(startOld, countOld, startNew, countNew) => DiffLine(line, 0) :: convertToDiffLines(lines.tail, startOld.toInt)
          case line => DiffLine(line, lineNumber) :: convertToDiffLines(lines.tail, lineNumber + 1)
        }
      }
    }

    val diffLines = diff.split("\n").toList

    convertToDiffLines(diffLines, 0)
  }
}

case class DiffLine(line: String, lineNumber: Int)
