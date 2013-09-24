package com.softwaremill.codebrag.dao.finders.commit

import com.typesafe.scalalogging.slf4j.Logging
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.CommitInfoRecord
import com.softwaremill.codebrag.common.LoadMoreCriteria
import com.foursquare.rogue.LiftRogue._
import com.softwaremill.codebrag.dao.reporting.views.CommitListView

class AllCommitsFinder extends CommitByIdFinder with UserDataEnhancer with CommitReviewedByUserMarker with Logging {

  import CommitInfoToViewConverter._
  import ListSliceLoader._
  import OutOfPageCommitCounter._

  def findAllCommits(paging: LoadMoreCriteria, userId: ObjectId) = {
    val allCommitsIds = CommitInfoRecord.select(_.id).orderAsc(_.committerDate).andAsc(_.authorDate).fetch()
    val commitsSlice = loadSliceUsing(paging, allCommitsIds, loadCommitsFn)
    val commits = toCommitViews(commitsSlice)
    val numOlder = countOlderCommits(allCommitsIds.map(_.toString), commits)
    val numNewer = countNewerCommits(allCommitsIds.map(_.toString), commits)
    enhanceWithUserData(markAsReviewed(commits, userId).copy(older = numOlder, newer = numNewer))
  }

  private def loadCommitsFn(ids: List[ObjectId]) = {
    partialCommitDetailsQuery.where(_.id in ids).orderAsc(_.committerDate).andAsc(_.authorDate).fetch().map(tupleToCommitDetails)
  }

}
