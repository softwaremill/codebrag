package com.softwaremill.codebrag.common.paging

import com.softwaremill.codebrag.common.paging.PagingCriteria.Direction

trait ListPageSlicer[T] { self: PagingCriteria[T] =>

   def extractPageFrom(fullList: List[T]): List[T] = {
     criteria match {
       case PagingCriteria(None, Direction.Left, limit) => loadLast(fullList)
       case PagingCriteria(None, Direction.Right, limit) => loadFirst(fullList)
       case PagingCriteria(Some(id), Direction.Right, limit) => loadContextual(fullList, id, boundsForNext)
       case PagingCriteria(Some(id), Direction.Left, limit) => loadContextual(fullList, id, boundsForPrevious)
       case PagingCriteria(Some(id), Direction.Radial, limit) => loadContextual(fullList, id, boundsForSurroundings)
     }
   }

   private def boundsForPrevious(givenIndex: Int) = (givenIndex - criteria.limit, givenIndex)
   private def boundsForNext(givenIndex: Int) = (givenIndex + 1, givenIndex + criteria.limit + 1)
   private def boundsForSurroundings(givenIndex: Int) = (givenIndex - criteria.limit, givenIndex + criteria.limit + 1)

   private def loadLast(fullList: List[T]): List[T] = {
     loadWithBounds(fullList, fullList.length, boundsForPrevious)
   }
   private def loadFirst(fullList: List[T]): List[T] = {
     loadWithBounds(fullList, -1, boundsForNext)
   }

   private def loadContextual(fullList: List[T], pivotId: T, boundsFn: (Int => (Int, Int))): List[T] = {
     loadWithBounds(fullList, fullList.indexOf(pivotId), boundsFn)
   }

   private def loadWithBounds(fullList: List[T], indexOfPivot: Int, boundsFn: (Int => (Int, Int))): List[T] = {
     val bounds: (Int, Int) = boundsFn(indexOfPivot)
     fullList.slice(bounds._1, bounds._2)
   }

   private def criteria = this

 }
