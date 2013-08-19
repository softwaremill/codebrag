package com.softwaremill.codebrag.dao.finders.commit

import com.typesafe.scalalogging.slf4j.Logging
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.{CommitInfoRecord, CommitReviewTaskRecord}
import com.softwaremill.codebrag.dao.reporting.views.CommitListView
import com.softwaremill.codebrag.common.PagingCriteria
import com.foursquare.rogue.LiftRogue._

class ReviewableCommitsListFinder extends CommitByIdFinder with UserDataEnhancer with CommitReviewedByUserMarker with Logging {

  import CommitInfoToViewConverter._
  import ListSliceLoader._

  def findCommitsToReviewFor(userId: ObjectId, paging: PagingCriteria) = {
    val commitsIdsToReview =  reviewableCommitsIds(userId)
    val commitsSlice = loadSliceUsing(paging, commitsIdsToReview, loadCommitsFn)
    val commits = toCommitViews(commitsSlice)
    CommitListView(enhanceWithUserData(commits), commitsIdsToReview.length)
  }

  private def loadCommitsFn(ids: List[ObjectId]) = {
    partialCommitDetailsQuery.where(_.id in ids).orderAsc(_.committerDate).andAsc(_.authorDate).fetch().map(tupleToCommitDetails)
  }

  private def reviewableCommitsIds(userId: ObjectId) = {
    val reviewTasksForUser = CommitReviewTaskRecord.where(_.userId eqs userId).fetch()
    CommitInfoRecord.select(_.id).where(_.id in reviewTasksForUser.map(_.commitId.get)).orderAsc(_.committerDate).andAsc(_.authorDate).fetch()
  }

}

