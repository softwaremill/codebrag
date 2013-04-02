package com.softwaremill.codebrag.service.diff

import com.softwaremill.codebrag.dao.CommitInfoDAO
import annotation.tailrec

class DiffService(commitInfoDao: CommitInfoDAO) {

  val Info = """@@ -(\d+),(\d+) \+(\d+),(\d+) @@""".r

  def parseDiff(diff: String): List[DiffLine] = {
    def lineToChange(line: String): String = {
      if (line.startsWith("+")) "added"
      else if (line.startsWith("-")) "removed"
      else "not-changed"
    }
    @tailrec
    def convertToDiffLines(lines: List[String], lineNumber: Int, accu: List[DiffLine]): List[DiffLine] = {
      if (lines.isEmpty) {
        accu.reverse
      } else {
        lines.head match {
          case line@Info(startOld, countOld, startNew, countNew) => convertToDiffLines(lines.tail, startOld.toInt, DiffLine(line, 0, "") :: accu)
          case line => convertToDiffLines(lines.tail, lineNumber + 1, DiffLine(line, lineNumber, lineToChange(line)) :: accu)
        }
      }
    }

    val diffLines = diff.split("\n").toList

    convertToDiffLines(diffLines, 0, List())
  }

  def getFilesWithDiffs(commitId: String): Either[String, List[FileWithDiff]] = {
    commitInfoDao.findBySha(commitId) match {
      case Some(commit) => Right(commit.files.map(file => FileWithDiff(file.filename, parseDiff(file.patch))))
      case None => Left("No such commit")
    }
  }
}

case class DiffLine(line: String, lineNumber: Int, change: String)

case class FileWithDiff(filename: String, lines: List[DiffLine])
