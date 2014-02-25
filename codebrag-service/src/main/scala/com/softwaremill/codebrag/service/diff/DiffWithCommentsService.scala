package com.softwaremill.codebrag.service.diff

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.finders.commit.AllCommitsFinder
import com.softwaremill.codebrag.dao.finders.views.CommitDetailsView
import com.softwaremill.codebrag.dao.finders.reaction.ReactionFinder

class DiffWithCommentsService(commitsFinder: AllCommitsFinder, reactionFinder: ReactionFinder, diffService: DiffService) {

  def diffWithCommentsFor(commitId: ObjectId, userId: ObjectId): Either[String, CommitDetailsView] = {
    for {
      commit <- commitsFinder.findCommitById(commitId, userId).right
      diff <- diffService.getFilesWithDiffs(commitId.toString).right
    } yield {
      val reactions = reactionFinder.findReactionsForCommit(commitId)
      CommitDetailsView.buildFrom(commit, reactions, diff)
    }
  }
}
