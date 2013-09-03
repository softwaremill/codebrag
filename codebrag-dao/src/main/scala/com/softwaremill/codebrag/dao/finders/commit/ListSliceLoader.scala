package com.softwaremill.codebrag.dao.finders.commit

import com.softwaremill.codebrag.common.LoadMoreCriteria
import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.LoadMoreCriteria.PagingDirection

object ListSliceLoader {

  def loadSliceUsing[T](criteria: LoadMoreCriteria, idsList: List[ObjectId], loadFn: (List[ObjectId] => List[T])) = {

    def boundsForPrevious(givenIndex: Int) = (givenIndex - criteria.limit, givenIndex)
    def boundsForNext(givenIndex: Int) = (givenIndex + 1, givenIndex + criteria.limit + 1)
    def boundsForSurroundings(givenIndex: Int) = (givenIndex - criteria.limit, givenIndex + criteria.limit + 1)

    criteria match {
      case LoadMoreCriteria(idOption@None, PagingDirection.Left, limit) => loadWithBounds(idsList, idsList.length, boundsForPrevious, loadFn)
      case LoadMoreCriteria(idOption@None, PagingDirection.Right, limit) => loadWithBounds(idsList, -1, boundsForNext, loadFn)
      case LoadMoreCriteria(Some(id), PagingDirection.Right, limit) => loadWithBounds(idsList, idsList.indexOf(id), boundsForNext, loadFn)
      case LoadMoreCriteria(Some(id), PagingDirection.Left, limit) => loadWithBounds(idsList, idsList.indexOf(id), boundsForPrevious, loadFn)
      case LoadMoreCriteria(Some(id), PagingDirection.Radial, limit) => loadWithBounds(idsList, idsList.indexOf(id), boundsForSurroundings, loadFn)
    }
  }

  private def loadWithBounds[T](idsList: List[ObjectId], indexOfPivot: Int, boundsFn: (Int => (Int, Int)), loadFn: (List[ObjectId] => List[T])): List[T] = {
    val bounds: (Int, Int) = boundsFn(indexOfPivot)
    loadFn(idsList.slice(bounds._1, bounds._2))
  }

}
