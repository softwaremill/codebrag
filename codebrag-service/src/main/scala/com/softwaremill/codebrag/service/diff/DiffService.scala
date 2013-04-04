package com.softwaremill.codebrag.service.diff

import com.softwaremill.codebrag.dao.CommitInfoDAO
import annotation.tailrec
import org.bson.types.ObjectId

class DiffService(commitInfoDao: CommitInfoDAO) {

  private val IrrelevantLineIndicator = -1

  val Info = """@@ -(\d+),(\d+) \+(\d+),(\d+) @@(.*)""".r

  def parseDiff(diff: String): List[DiffLine] = {
    @tailrec
    def convertToDiffLines(lines: List[String], lineNumberOriginal: Int, lineNumberChanged: Int, accu: List[DiffLine]): List[DiffLine] = {
      if (lines.isEmpty) {
        accu.reverse
      } else {
        lines.head match {
          case line@Info(startOld, countOld, startNew, countNew, rest) => convertToDiffLines(lines.tail, startOld.toInt, startNew.toInt, DiffLine(line, IrrelevantLineIndicator, IrrelevantLineIndicator, "header") :: accu)
          case line => {
            val lineChange = line.substring(0, 1)
            lineChange match {
              case "-" => convertToDiffLines(lines.tail, lineNumberOriginal + 1, lineNumberChanged, DiffLine(line, lineNumberOriginal, IrrelevantLineIndicator, "removed") :: accu)
              case "+" => convertToDiffLines(lines.tail, lineNumberOriginal, lineNumberChanged + 1, DiffLine(line, IrrelevantLineIndicator, lineNumberChanged, "added") :: accu)
              case _ => convertToDiffLines(lines.tail, lineNumberOriginal + 1, lineNumberChanged + 1, DiffLine(line, lineNumberOriginal, lineNumberChanged, "not-changed") :: accu)
            }
          }
        }
      }
    }

    val diffLines = diff.split("\n").toList

    convertToDiffLines(diffLines, 0, 0, List())
  }

  def getFilesWithDiffs(commitId: String): Either[String, List[FileWithDiff]] = {
    commitInfoDao.findByCommitId(new ObjectId(commitId)) match {
      case Some(commit) => Right(commit.files.map(file => FileWithDiff(file.filename, parseDiff(file.patch))))
      case None => Left("No such commit")
    }
  }
}

case class DiffLine(line: String, lineNumberOriginal: Int, lineNumberChanged: Int, lineType: String)

case class FileWithDiff(filename: String, lines: List[DiffLine])
