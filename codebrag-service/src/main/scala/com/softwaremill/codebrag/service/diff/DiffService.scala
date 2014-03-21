package com.softwaremill.codebrag.service.diff

import annotation.tailrec
import com.softwaremill.codebrag.domain.{FileDiffStats, DiffLine, CommitFileDiff}
import com.softwaremill.codebrag.service.commits.DiffLoader
import com.softwaremill.codebrag.repository.Repository

class DiffService(diffLoader: DiffLoader, repository: Repository) {

  val IrrelevantLineIndicator = -1
  val LineTypeHeader = "header"
  val LineTypeRemoved = "removed"
  val LineTypeAdded = "added"
  val LineTypeNotChanged = "not-changed"
  val Info = """@@ -(\d+),(\d+) \+(\d+),(\d+) @@(.*)""".r
  val PatchPattern =  """(?s)([^@]*)(@@ .*)""".r

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
      case Some(d) => {
        if (d.isEmpty) List() else d.split("\n").toList
      }
      case None => List.empty
    }

    convertToDiffLines(diffLines, 0, 0, List())
  }

  private def cutGitHeaders(patch: String) = {
    patch match {
      case PatchPattern(header, rest) => rest
      case _ => patch
    }
  }

  def getFilesWithDiffs(sha: String): Either[String, List[CommitFileDiff]] = {
    val result = for {
      diff <- diffLoader.loadDiff(sha, repository)
    } yield Right(diff.map(file => {
        val patch = cutGitHeaders(file.patch)
        val diffLines = parseDiff(patch)
        val lineTypeCounts = diffLines.groupBy(_.lineType).map(group => (group._1, group._2.size))
        CommitFileDiff(file.filename, file.status, diffLines, FileDiffStats(lineTypeCounts.getOrElse(LineTypeAdded, 0), lineTypeCounts.getOrElse(LineTypeRemoved, 0)))
      }))
    result.getOrElse(Left("No such commit"))
  }
}