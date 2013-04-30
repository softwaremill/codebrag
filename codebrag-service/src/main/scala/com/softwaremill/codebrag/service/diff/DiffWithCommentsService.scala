package com.softwaremill.codebrag.service.diff

import com.softwaremill.codebrag.dao.reporting.{CommitListItemDTO, CommitDetailsWithComments, CommentListFinder, CommitListFinder}
import org.bson.types.ObjectId

class DiffWithCommentsService(commitListFinder: CommitListFinder, commentsFinder: CommentListFinder, diffService: DiffService) {

  def diffWithCommentsFor(commitId: ObjectId): Either[String, CommitDetailsWithComments] = {

    def buildDiffWithComments(commit: CommitListItemDTO) = {
      val commitComments = commentsFinder.commentsForCommit(commitId)
      val Right(diff) = diffService.getFilesWithDiffs(commitId.toString)
      CommitDetailsWithComments.buildFrom(commit, commitComments, diff)
    }

    commitListFinder.findCommitInfoById(commitId.toString) match {
      case Right(commit) => Right(buildDiffWithComments(commit))
      case Left(msg) => Left(msg)
    }
  }


}
