package com.softwaremill.codebrag.dao.reporting.views

import com.softwaremill.codebrag.domain.{FileDiffStats, DiffLine, CommitFileDiff}
import CommentsView._

case class CommitDetailsWithCommentsView(
                                          commit: CommitView,
                                          diff: List[FileDiffView],
                                          supressedFiles: List[SupressedFileView],
                                          comments: List[SingleCommentView],
                                          inlineComments: Map[String, CommitDetailsWithCommentsView.StrLineToCommentListMap])

case class FileDiffView(filename: String, status: String, lines: List[DiffLineView], diffStats: FileDiffStatsView)
case class SupressedFileView(filename: String, status: String, diffStats: FileDiffStatsView)
case class FileDiffStatsView(added: Int, removed: Int)
case class DiffLineView(line: String, lineNumberOriginal: String, lineNumberChanged: String, lineType: String)


object DiffLineView {

  val HeaderLine = ("...", "...")

  def fromDiffLine(diffLine: DiffLine) = {
    DiffLineView(diffLine.line, diffLine.lineNumberOriginal, diffLine.lineNumberChanged, diffLine.lineType)
  }

  def apply(line: String, lineNumberOriginal: Int, lineNumberChanged: Int, lineType: String) = {

    def lineAdded(num: Int) = ("", num.toString)
    def lineRemoved(num: Int) = (num.toString, "")
    def lineNotChanged(orig: Int, changed: Int) = (orig.toString, changed.toString)

    val lines = (lineNumberOriginal, lineNumberChanged) match {
      case (-1, -1) =>  HeaderLine
      case (-1, num) => lineAdded(num)
      case (num, -1) => lineRemoved(num)
      case (orig, changed) => lineNotChanged(orig, changed)
    }
    new DiffLineView(line, lines._1, lines._2, lineType)
  }
}


object CommitDetailsWithCommentsView {

  val MaxAcceptableDiffLinesCount = 600
  type StrLineToCommentListMap = Map[String, List[SingleCommentView]]

  def buildFrom(commit: CommitView, comments: CommentsView, diffs: List[CommitFileDiff]) = {
    val stringifiedCommentsMap = mapKeysToString(comments)
    val (smallerDiffs, largerDiffs) = diffs.partition(_.lines.size < MaxAcceptableDiffLinesCount)
    CommitDetailsWithCommentsView(commit, buildDiffView(smallerDiffs), buildSupressedDiffView(largerDiffs), comments.comments, stringifiedCommentsMap)
  }

  private def mapKeysToString(comments: CommentsView): Map[String, StrLineToCommentListMap] = {
    val stringified = comments.inlineComments.map({
      case (fileName, fileComments) =>
        val withLineNumbersAsStrings = fileComments.map({
          case (lineNumber, lineComments) =>
            (lineNumber.toString, lineComments)
        })
        (fileName, withLineNumbersAsStrings)
    })
    stringified
  }

  private def buildDiffView(diffs: List[CommitFileDiff]) = {
    diffs.map({fileDiff =>
      val lineViews = fileDiff.lines.map(DiffLineView.fromDiffLine(_))
      FileDiffView(fileDiff.filename, fileDiff.status, lineViews, FileDiffStatsView(fileDiff.diffStats))
    })
  }

  private def buildSupressedDiffView(diffs: List[CommitFileDiff]) = {
    diffs.map({fileDiff =>
      SupressedFileView(fileDiff.filename, fileDiff.status, FileDiffStatsView(fileDiff.diffStats))
    })
  }

}

object FileDiffStatsView {

  def apply(diffStats: FileDiffStats) = {
      new FileDiffStatsView(diffStats.added, diffStats.removed)
  }

}
