package com.softwaremill.codebrag.service.diff

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.finders.commit.AllCommitsFinder
import com.softwaremill.codebrag.dao.finders.views.{CommitView, CommitDetailsView}
import com.softwaremill.codebrag.dao.finders.reaction.ReactionFinder

class DiffWithCommentsService(commitsFinder: AllCommitsFinder, reactionFinder: ReactionFinder, diffService: DiffService) {

  def diffWithCommentsFor(commitId: ObjectId, userId: ObjectId): Either[String, CommitDetailsView] = {

    def buildDiffWithComments(commit: CommitView) = {
      val reactions = reactionFinder.findReactionsForCommit(commitId)
      val Right(diff) = diffService.getFilesWithDiffs(commitId.toString)
      CommitDetailsView.buildFrom(commit, reactions, diff)
    }

    commitsFinder.findCommitById(commitId, userId) match {
      case Right(commit) => Right(buildDiffWithComments(commit))
      case Left(msg) => Left(msg)
    }
  }


}
