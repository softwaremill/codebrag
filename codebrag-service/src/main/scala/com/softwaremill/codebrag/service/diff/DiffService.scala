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
    def convertToDiffLines(lines: List[String], lineNumberOriginal: Int, lineNumberChanged: Int, accu: List[DiffLine]): List[DiffLine] = {
      if (lines.isEmpty) {
        accu.reverse
      } else {
        lines.head match {
          case line@Info(startOld, countOld, startNew, countNew) => convertToDiffLines(lines.tail, startOld.toInt, startNew.toInt, DiffLine(line, "0", "0", "") :: accu)
          case line => {
            val lineChange = line.substring(0, 1)
            val changeType: String = lineToChange(line)
            lineChange match {
              case "-" => convertToDiffLines(lines.tail, lineNumberOriginal + 1, lineNumberChanged, DiffLine(line, lineNumberOriginal.toString, "", changeType) :: accu)
              case "+" => convertToDiffLines(lines.tail, lineNumberOriginal, lineNumberChanged + 1, DiffLine(line, "", lineNumberChanged.toString, changeType) :: accu)
              case _ => convertToDiffLines(lines.tail, lineNumberOriginal + 1, lineNumberChanged + 1, DiffLine(line, lineNumberOriginal.toString, lineNumberChanged.toString, changeType) :: accu)
            }
          }
        }
      }
    }

    val diffLines = diff.split("\n").toList

    convertToDiffLines(diffLines, 0, 0, List())
  }

  def getFilesWithDiffs(commitId: String): Either[String, List[FileWithDiff]] = {
    commitInfoDao.findBySha(commitId) match {
      case Some(commit) => Right(commit.files.map(file => FileWithDiff(file.filename, parseDiff(file.patch))))
      case None => Left("No such commit")
    }
  }
}

case class DiffLine(line: String, lineNumberOriginal: String, lineNumberChanged: String, change: String)

case class FileWithDiff(filename: String, lines: List[DiffLine])
