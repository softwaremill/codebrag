package com.softwaremill.codebrag.dao.finders.commit

import com.softwaremill.codebrag.common.{LoadMoreCriteria}
import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.LoadMoreCriteria.PagingDirection

object ListSliceLoader {

  def loadSliceUsing[T](criteria: LoadMoreCriteria, idsList: List[ObjectId], loadFn: (List[ObjectId] => List[T])) = {

    def boundsForPrevious(givenIndex: Int) = (givenIndex - criteria.limit, givenIndex)
    def boundsForNext(givenIndex: Int) = (givenIndex + 1, givenIndex + criteria.limit + 1)
    def boundsForSurroundings(givenIndex: Int) = (givenIndex - criteria.limit, givenIndex + criteria.limit + 1)

    criteria.direction match {
      case PagingDirection.Left => loadWithBounds(idsList, criteria.baseId, criteria.limit, boundsForPrevious, loadFn)
      case PagingDirection.Right => loadWithBounds(idsList, criteria.baseId, criteria.limit, boundsForNext, loadFn)
      case PagingDirection.Radial => loadWithBounds(idsList, criteria.baseId, criteria.limit, boundsForSurroundings, loadFn)
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
