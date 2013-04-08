package com.softwaremill.codebrag.service.diff

import com.softwaremill.codebrag.dao.CommitInfoDAO
import annotation.tailrec
import org.bson.types.ObjectId

class DiffService(commitInfoDao: CommitInfoDAO) {

  val IrrelevantLineIndicator = -1
  val LineTypeHeader = "header"
  val LineTypeRemoved = "removed"
  val LineTypeAdded = "added"
  val LineTypeNotChanged = "not-changed"

  val Info = """@@ -(\d+),(\d+) \+(\d+),(\d+) @@(.*)""".r

  def parseDiff(diff: String): List[DiffLine] = {
    @tailrec
    def convertToDiffLines(lines: List[String], lineNumberOriginal: Int, lineNumberChanged: Int, accu: List[DiffLine]): List[DiffLine] = {
      if (lines.isEmpty) {
        accu.reverse
      } else {
        lines.head match {
          case line@Info(startOld, countOld, startNew, countNew, rest) => convertToDiffLines(lines.tail, startOld.toInt, startNew.toInt, DiffLine(line, IrrelevantLineIndicator, IrrelevantLineIndicator, LineTypeHeader) :: accu)
          case line => {
            val lineChange = line.substring(0, 1)
            lineChange match {
              case "-" => convertToDiffLines(lines.tail, lineNumberOriginal + 1, lineNumberChanged, DiffLine(line, lineNumberOriginal, IrrelevantLineIndicator, LineTypeRemoved) :: accu)
              case "+" => convertToDiffLines(lines.tail, lineNumberOriginal, lineNumberChanged + 1, DiffLine(line, IrrelevantLineIndicator, lineNumberChanged, LineTypeAdded) :: accu)
              case _ => convertToDiffLines(lines.tail, lineNumberOriginal + 1, lineNumberChanged + 1, DiffLine(line, lineNumberOriginal, lineNumberChanged, LineTypeNotChanged) :: accu)
            }
          }
        }
      }
    }

    val diffLines = Option(diff) match {
      case Some(d) => if (d.isEmpty) List() else d.split("\n").toList
      case None => List.empty
    }

    convertToDiffLines(diffLines, 0, 0, List())
  }

  def getFilesWithDiffs(commitId: String): Either[String, List[FileWithDiff]] = {
    commitInfoDao.findByCommitId(new ObjectId(commitId)) match {
      case Some(commit) => Right(commit.files.map(file => FileWithDiff(file.filename, file.status, parseDiff(file.patch))))
      case None => Left("No such commit")
    }
  }
}

case class DiffLine(line: String, lineNumberOriginal: Int, lineNumberChanged: Int, lineType: String)

case class FileWithDiff(filename: String, status: String, lines: List[DiffLine])
