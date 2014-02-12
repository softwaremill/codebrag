package com.softwaremill.codebrag.dao.finders.views

import com.softwaremill.codebrag.domain.{FileDiffStats, DiffLine, CommitFileDiff}
import com.softwaremill.codebrag.dao.finders.views.ReactionsView

case class CommitDetailsView(
                            commit: CommitView,
                            diff: List[FileDiffView],
                            supressedFiles: List[SupressedFileView],
                            reactions: ReactionsView,
                            lineReactions: Map[String, Map[String, ReactionsView]])

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

object CommitDetailsView {

  val MaxAcceptableDiffLinesCount = 600

  def buildFrom(commit: CommitView, reactions: CommitReactionsView, diffs: List[CommitFileDiff]) = {
    val (smallerDiffs, largerDiffs) = diffs.partition(_.lines.size < MaxAcceptableDiffLinesCount)
    CommitDetailsView(
        commit,
        buildDiffView(smallerDiffs),
        buildSupressedDiffView(largerDiffs),
        reactions.entireCommitReactions,
        reactions.inlineReactions)
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
