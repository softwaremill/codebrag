package com.softwaremill.codebrag.dao.reporting.views

import com.softwaremill.codebrag.domain.{FileDiffStats, DiffLine, CommitFileDiff}


case class CommitDetailsWithCommentsView(commit: CommitView, diff: List[FileDiffView], comments: List[SingleCommentView], inlineComments: Map[String, Map[String, List[SingleCommentView]]])
case class FileDiffView(filename: String, status: String, lines: List[DiffLineView], diffStats: FileDiffStatsView)
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

  def buildFrom(commit: CommitView, comments: CommentsView, diffs: List[CommitFileDiff]) = {
    val stringified = comments.inlineComments.map({ fileComments =>
      val withLineNumbersAsStrings = fileComments._2.map({ lineComments =>
        (lineComments._1.toString, lineComments._2)
      })
      (fileComments._1, withLineNumbersAsStrings)
    })
    CommitDetailsWithCommentsView(commit, buildDiffView(diffs), comments.comments, stringified)
  }

  def buildDiffView(diffs: List[CommitFileDiff]) = {
    diffs.map({fileDiff =>
      val lineViews  =fileDiff.lines.map(DiffLineView.fromDiffLine(_))
      FileDiffView(fileDiff.filename, fileDiff.status, lineViews, FileDiffStatsView(fileDiff.diffStats))
    })
  }

}

object FileDiffStatsView {

  def apply(diffStats: FileDiffStats) = {
      new FileDiffStatsView(diffStats.added, diffStats.removed)
  }

}
