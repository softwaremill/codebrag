package com.softwaremill.codebrag.dao.reporting.views

import com.softwaremill.codebrag.domain.{DiffLine, CommitFileDiff}


case class CommitDetailsWithCommentsView(commit: CommitView, diff: List[FileDiffView], comments: List[SingleCommentView], inlineComments: Map[String, Map[Int, List[SingleCommentView]]])
case class FileDiffView(filename: String, status: String, lines: List[DiffLineView])
case class DiffLineView(line: String, lineNumberOriginal: String, lineNumberChanged: String, lineType: String)


object DiffLineView {
  def fromDiffLine(diffLine: DiffLine) = {
    DiffLineView(diffLine.line, diffLine.lineNumberOriginal, diffLine.lineNumberChanged, diffLine.lineType)
  }

  def apply(line: String, lineNumberOriginal: Int, lineNumberChanged: Int, lineType: String) = {
    val lines = (lineNumberOriginal, lineNumberChanged) match {
      case (-1, num) => ("", num.toString)
      case (num, -1) => (num.toString, "")
      case (orig, changed) => (orig.toString, changed.toString)
    }
    new DiffLineView(line, lines._1, lines._2, lineType)
  }
}


object CommitDetailsWithCommentsView {

  def buildFrom(commit: CommitView, comments: CommentsView, diffs: List[CommitFileDiff]) = {
    CommitDetailsWithCommentsView(commit, buildDiffView(diffs), comments.comments, comments.inlineComments)
  }

  def buildDiffView(diffs: List[CommitFileDiff]) = {
    diffs.map({fileDiff =>
      val lineViews  =fileDiff.lines.map(DiffLineView.fromDiffLine(_))
      FileDiffView(fileDiff.filename, fileDiff.status, lineViews)
    })
  }

}
