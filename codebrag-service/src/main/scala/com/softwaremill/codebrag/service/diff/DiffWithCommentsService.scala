package com.softwaremill.codebrag.service.diff

import com.softwaremill.codebrag.dao.reporting.{CommentFinder, CommitFinder}
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.reporting.views.{CommitView, CommitDetailsWithCommentsView}

class DiffWithCommentsService(commitListFinder: CommitFinder, commentsFinder: CommentFinder, diffService: DiffService) {

  def diffWithCommentsFor(commitId: ObjectId): Either[String, CommitDetailsWithCommentsView] = {

    def buildDiffWithComments(commit: CommitView) = {
      val commitComments = commentsFinder.commentsForCommit(commitId)
      val Right(diff) = diffService.getFilesWithDiffs(commitId.toString)
      CommitDetailsWithCommentsView.buildFrom(commit, commitComments, diff)
    }

    commitListFinder.findCommitInfoById(commitId.toString) match {
      case Right(commit) => Right(buildDiffWithComments(commit))
      case Left(msg) => Left(msg)
    }
  }


}
