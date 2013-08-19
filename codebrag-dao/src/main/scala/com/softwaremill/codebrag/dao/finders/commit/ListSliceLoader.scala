package com.softwaremill.codebrag.dao.finders.commit

import com.softwaremill.codebrag.common.{PagingCriteria, SurroundingsCriteria}
import org.bson.types.ObjectId

object ListSliceLoader {

  def loadSurroundingSliceUsing[T](criteria: SurroundingsCriteria, idsList: List[ObjectId], loadFn: (List[ObjectId] => List[T])) = {

    def boundsForSurroundings(givenIndex: Int) = (givenIndex - criteria.loadLimit, givenIndex + criteria.loadLimit + 1)
    loadWithBounds(idsList, Some(criteria.commitId), criteria.loadLimit, boundsForSurroundings, loadFn)
  }

  def loadSliceUsing[T](criteria: PagingCriteria, idsList: List[ObjectId], loadFn: (List[ObjectId] => List[T])) = {

    def boundsForPreviousCommits(givenIndex: Int) = (givenIndex - criteria.limit, givenIndex)
    def boundsForNextCommits(givenIndex: Int) = (givenIndex + 1, givenIndex + criteria.limit + 1)

    if(criteria.maxCommitId.isDefined) {
      loadWithBounds(idsList, criteria.maxCommitId, criteria.limit, boundsForPreviousCommits, loadFn)
    } else if(criteria.minCommitId.isDefined) {
      loadWithBounds(idsList, criteria.minCommitId, criteria.limit, boundsForNextCommits, loadFn)
    } else {
      val noPivotId = None
      loadWithBounds(idsList, noPivotId, criteria.limit, boundsForNextCommits, loadFn)
    }

  }

  private def loadWithBounds[T](idsList: List[ObjectId], pivotId: Option[ObjectId], limit: Int, boundsFn: (Int => (Int, Int)), loadFn: (List[ObjectId] => List[T])): List[T] = {
    val bounds = pivotId match {
      case Some(id) => {
        val indexOfPivot = idsList.indexOf(id)
        if(indexOfPivot > -1) boundsFn(indexOfPivot) else (0, 0)
      }
      case None => boundsFn(-1)
    }
    loadFn(idsList.slice(bounds._1, bounds._2))
  }

}
