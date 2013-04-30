package com.softwaremill.codebrag.dao.reporting

import com.softwaremill.codebrag.domain.CommitFileDiff


case class DiffLineWithComments(line: String, lineNumberOriginal: Int, lineNumberChanged: Int, lineType: String, comments: List[SingleCommentView])
case class CommitFileDiffWithComments(filename: String, status: String, lines: List[DiffLineWithComments])

case class CommitDetailsWithComments(commit: CommitListItemDTO, comments: List[SingleCommentView], files: List[CommitFileDiffWithComments])

object CommitDetailsWithComments {

  def buildFrom(commit: CommitListItemDTO, comments: CommentsView, diffs: List[CommitFileDiff]) = {
    val filesWithComments = joinFilesWithComments(diffs, comments)
    new CommitDetailsWithComments(commit, comments.comments, filesWithComments)
  }

  private def joinFilesWithComments(diffs: List[CommitFileDiff], comments: CommentsView): List[CommitFileDiffWithComments] = {
    diffs.map(diff => {
      val linesWithComments = joinCommentsWithLine(diff, comments.inlineComments)
      CommitFileDiffWithComments(diff.filename, diff.status, linesWithComments)
    }).toList
  }

  private def joinCommentsWithLine(file: CommitFileDiff, comments: List[FileCommentsView]) = {
    file.lines.view.zipWithIndex.map(line => {
      val commentsForLine = findCommentsForLine(file.filename, line._2, comments)
      DiffLineWithComments(line._1.line, line._1.lineNumberOriginal, line._1.lineNumberChanged, line._1.lineType, commentsForLine)
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
