package com.softwaremill.codebrag.common.paging

import com.softwaremill.codebrag.common.paging.PagingCriteria.Direction


case class PagingCriteria[T](baseId: Option[T], direction: Direction.Value, limit: Int) extends ListPageSlicer[T]

object PagingCriteria {

  def apply[T](baseId: T, direction: Direction.Value, limit: Int) = {
    new PagingCriteria(Some(baseId), direction, limit)
  }

  def fromBeginning[T](limit: Int) = {
    new PagingCriteria[T](None, Direction.Right, limit)
  }

  def fromEnd[T](limit: Int) = {
    new PagingCriteria[T](None, Direction.Left, limit)
  }

  object Direction extends Enumeration {
    type PagingDirection = Value
    val Left, Right, Radial = Value
  }
}

