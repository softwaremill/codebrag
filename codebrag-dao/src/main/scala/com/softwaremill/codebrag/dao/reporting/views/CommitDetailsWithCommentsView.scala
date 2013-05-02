package com.softwaremill.codebrag.dao.reporting.views

import com.softwaremill.codebrag.domain.CommitFileDiff


case class DiffLineWithCommentsView(line: String, lineNumberOriginal: Int, lineNumberChanged: Int, lineType: String, comments: List[SingleCommentView])
case class CommitFileDiffWithCommentsView(filename: String, status: String, lines: List[DiffLineWithCommentsView])

case class CommitDetailsWithCommentsView(commit: CommitView, comments: List[SingleCommentView], files: List[CommitFileDiffWithCommentsView])

object CommitDetailsWithCommentsView {

  def buildFrom(commit: CommitView, comments: CommentsView, diffs: List[CommitFileDiff]) = {
    val filesWithComments = joinFilesWithComments(diffs, comments)
    new CommitDetailsWithCommentsView(commit, comments.comments, filesWithComments)
  }

  private def joinFilesWithComments(diffs: List[CommitFileDiff], comments: CommentsView): List[CommitFileDiffWithCommentsView] = {
    diffs.map(diff => {
      val linesWithComments = joinCommentsWithLine(diff, comments.inlineComments)
      CommitFileDiffWithCommentsView(diff.filename, diff.status, linesWithComments)
    }).toList
  }

  private def joinCommentsWithLine(file: CommitFileDiff, comments: List[FileCommentsView]) = {
    file.lines.view.zipWithIndex.map(line => {
      val commentsForLine = findCommentsForLine(file.filename, line._2, comments)
      DiffLineWithCommentsView(line._1.line, line._1.lineNumberOriginal, line._1.lineNumberChanged, line._1.lineType, commentsForLine)
    }).toList
  }

  private def findCommentsForLine(fileName: String, lineNumber: Int, comments: List[FileCommentsView]) = {
    comments.find(_.fileName == fileName) match {
      case Some(fileComments) => {
        fileComments.lineComments.get(lineNumber).getOrElse(List[SingleCommentView]())
      }
      case None => List[SingleCommentView]()
    }
  }

}
