package com.softwaremill.codebrag.service.diff

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.finders.views.CommitDetailsView
import com.softwaremill.codebrag.dao.finders.reaction.ReactionFinder
import com.softwaremill.codebrag.finders.commits.all.AllCommitsFinder

class DiffWithCommentsService(commitsFinder: AllCommitsFinder, reactionFinder: ReactionFinder, diffService: DiffService) {

  def diffWithCommentsFor(repoName: String, sha: String, userId: ObjectId): Either[String, CommitDetailsView] = {
    commitsFinder.findSingle(repoName, sha, userId) match {
      case Right(commit) => {
        for {
          diff <- diffService.getFilesWithDiffs(sha).right
        } yield {
          val reactions = reactionFinder.findReactionsForCommit(new ObjectId(commit.id))
          CommitDetailsView.buildFrom(commit, reactions, diff)
        }
      }
      case Left(error) => Left(error)
    }
  }
}
