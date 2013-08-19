package com.softwaremill.codebrag.dao.finders.commit

import com.typesafe.scalalogging.slf4j.Logging
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.CommitInfoRecord
import com.softwaremill.codebrag.common.LoadMoreCriteria
import com.foursquare.rogue.LiftRogue._

class AllCommitsFinder extends CommitByIdFinder with UserDataEnhancer with CommitReviewedByUserMarker with Logging {

  import CommitInfoToViewConverter._
  import ListSliceLoader._

  def findAllCommits(paging: LoadMoreCriteria, userId: ObjectId) = {
    val allCommitsIds = CommitInfoRecord.select(_.id).orderAsc(_.committerDate).andAsc(_.authorDate).fetch()
    val commitsSlice = loadSliceUsing(paging, allCommitsIds, loadCommitsFn)
    enhanceWithUserData(markAsReviewed(toCommitViews(commitsSlice), userId))
  }

  def findWithSurroundings(criteria: LoadMoreCriteria, userId: ObjectId) = {
    val allCommits = CommitInfoRecord.select(_.id).orderAsc(_.committerDate).andAsc(_.authorDate).fetch()
    val commitsSlice = loadSliceUsing(criteria, allCommits, loadCommitsFn)
    val commits = toCommitViews(commitsSlice)
    enhanceWithUserData(markAsReviewed(commits, userId))
  }

  private def loadCommitsFn(ids: List[ObjectId]) = {
    partialCommitDetailsQuery.where(_.id in ids).orderAsc(_.committerDate).andAsc(_.authorDate).fetch().map(tupleToCommitDetails)
  }

}
