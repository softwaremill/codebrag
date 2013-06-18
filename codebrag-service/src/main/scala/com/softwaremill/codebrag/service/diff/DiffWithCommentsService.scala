package com.softwaremill.codebrag.service.diff

import com.softwaremill.codebrag.dao.reporting.{UserReactionFinder, CommitFinder}
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.reporting.views.{CommitView, CommitDetailsView}

class DiffWithCommentsService(commitListFinder: CommitFinder, reactionFinder: UserReactionFinder, diffService: DiffService) {

  def diffWithCommentsFor(commitId: ObjectId, userId: ObjectId): Either[String, CommitDetailsView] = {

    def buildDiffWithComments(commit: CommitView) = {
      val reactions = reactionFinder.findReactionsForCommit(commitId)
      val Right(diff) = diffService.getFilesWithDiffs(commitId.toString)
      CommitDetailsView.buildFrom(commit, reactions, diff)
    }

    commitListFinder.findCommitInfoById(commitId.toString, userId) match {
      case Right(commit) => Right(buildDiffWithComments(commit))
      case Left(msg) => Left(msg)
    }
  }


}
