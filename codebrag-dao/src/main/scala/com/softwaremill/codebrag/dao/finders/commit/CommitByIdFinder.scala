package com.softwaremill.codebrag.dao.finders.commit

import org.bson.types.ObjectId
import scala.Some
import com.foursquare.rogue.LiftRogue._

trait CommitByIdFinder {

  self: CommitReviewedByUserMarker with UserDataEnhancer =>

  import CommitInfoToViewConverter._

  def findCommitById(commitId: ObjectId, userId: ObjectId) = {
    val commitOption = partialCommitDetailsQuery.where(_.id eqs commitId).get()
    commitOption match {
      case Some(record) => {
        val commit = tupleToCommitDetails(record)
        Right(markAsReviewed(enhanceWithUserData(toCommitView(commit)), userId))
      }
      case None => Left(s"No such commit ${commitId.toString}")
    }
  }

}
