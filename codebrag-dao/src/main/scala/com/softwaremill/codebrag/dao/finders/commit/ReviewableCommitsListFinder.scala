package com.softwaremill.codebrag.dao.finders.commit

import com.typesafe.scalalogging.slf4j.Logging
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.{CommitReviewTaskRecord}
import com.softwaremill.codebrag.dao.reporting.views.{CommitView, CommitListView}
import com.softwaremill.codebrag.common.LoadMoreCriteria
import com.foursquare.rogue.LiftRogue._
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoRecord

class ReviewableCommitsListFinder extends CommitByIdFinder with UserDataEnhancer with CommitReviewedByUserMarker with Logging {

  import CommitInfoToViewConverter._
  import ListSliceLoader._
  import OutOfPageCommitCounter._

  def findCommitsToReviewFor(userId: ObjectId, paging: LoadMoreCriteria) = {
    val commitsIdsToReview = reviewableCommitsIds(userId)
    val commitsSlice = loadSliceUsing(paging, commitsIdsToReview, loadCommitsFn)
    val commits = toCommitViews(commitsSlice)
    val numOlder = countOlderCommits(commitsIdsToReview.map(_.toString), commits)
    val numNewer = countNewerCommits(commitsIdsToReview.map(_.toString), commits)
    CommitListView(enhanceWithUserData(commits), numOlder, numNewer)
  }

  private def loadCommitsFn(ids: List[ObjectId]) = {
    partialCommitDetailsQuery.where(_.id in ids).orderAsc(_.committerDate).andAsc(_.authorDate).fetch().map(tupleToCommitDetails)
  }

  private def reviewableCommitsIds(userId: ObjectId) = {
    val reviewTasksForUser = CommitReviewTaskRecord.where(_.userId eqs userId).fetch()
    CommitInfoRecord.select(_.id).where(_.id in reviewTasksForUser.map(_.commitId.get)).orderAsc(_.committerDate).andAsc(_.authorDate).fetch()
  }

}


