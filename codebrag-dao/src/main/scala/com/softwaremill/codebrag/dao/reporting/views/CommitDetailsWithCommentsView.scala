package com.softwaremill.codebrag.dao.reporting.views

import com.softwaremill.codebrag.domain.{FileDiffStats, DiffLine, CommitFileDiff}


case class CommitDetailsWithCommentsView(
                                          commit: CommitView,
                                          diff: List[FileDiffView],
                                          supressedFiles: List[SupressedFileView],
                                          comments: List[SingleCommentView],
                                          inlineComments: Map[String, Map[String, List[SingleCommentView]]])

case class FileDiffView(filename: String, status: String, lines: List[DiffLineView], diffStats: FileDiffStatsView)
case class SupressedFileView(filename: String, status: String, diffStats: FileDiffStatsView)
case class FileDiffStatsView(added: Int, removed: Int)
case class DiffLineView(line: String, lineNumberOriginal: String, lineNumberChanged: String, lineType: String)


object DiffLineView {
  def fromDiffLine(diffLine: DiffLine) = {
    DiffLineView(diffLine.line, diffLine.lineNumberOriginal, diffLine.lineNumberChanged, diffLine.lineType)
  }

  def apply(line: String, lineNumberOriginal: Int, lineNumberChanged: Int, lineType: String) = {
    val lines = (lineNumberOriginal, lineNumberChanged) match {
      case (-1, -1) => ("...", "...")   //header line
      case (-1, num) => ("", num.toString)    // line added
      case (num, -1) => (num.toString, "")    // line removed
      case (orig, changed) => (orig.toString, changed.toString)   // line not changed
    }
    new DiffLineView(line, lines._1, lines._2, lineType)
  }
}


object CommitDetailsWithCommentsView {

  val MaxAcceptableDiffLinesNumber = 600

  def buildFrom(commit: CommitView, comments: CommentsView, diffs: List[CommitFileDiff]) = {
    val stringifiedCommentsMap = mapKeysToString(comments)
    val (smallerDiffs, largerDiffs) = diffs.partition(_.lines.size < MaxAcceptableDiffLinesNumber)
    CommitDetailsWithCommentsView(commit, buildDiffView(smallerDiffs), buildSupressedDiffView(largerDiffs), comments.comments, stringifiedCommentsMap)
  }


  private def mapKeysToString(comments: CommentsView): Map[String, Map[String, List[SingleCommentView]]] = {
    val stringified = comments.inlineComments.map({
      fileComments =>
        val withLineNumbersAsStrings = fileComments._2.map({
          lineComments =>
            (lineComments._1.toString, lineComments._2)
        })
        (fileComments._1, withLineNumbersAsStrings)
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
