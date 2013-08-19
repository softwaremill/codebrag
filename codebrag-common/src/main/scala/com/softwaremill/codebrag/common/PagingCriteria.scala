package com.softwaremill.codebrag.common

import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.LoadMoreCriteria.PagingDirection

case class LoadMoreCriteria(baseId: Option[ObjectId], direction: PagingDirection.Value, limit: Int)

object LoadMoreCriteria {

  def apply(baseId: ObjectId, direction: PagingDirection.Value, limit: Int) = {
    new LoadMoreCriteria(Some(baseId), direction, limit)
  }

  object PagingDirection extends Enumeration {
    type PagingDirection = Value
    val Left, Right, Radial = Value
  }
}