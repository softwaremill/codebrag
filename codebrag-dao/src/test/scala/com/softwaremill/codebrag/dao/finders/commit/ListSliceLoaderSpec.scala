package com.softwaremill.codebrag.dao.finders.commit

import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.common.{LoadMoreCriteria}
import com.softwaremill.codebrag.dao.ObjectIdTestUtils._
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.ObjectIdTestUtils
import LoadMoreCriteria.PagingDirection

class ListSliceLoaderSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {

  val elements = listOfObjectId(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)

  def loadElementsFn(ids: List[ObjectId]) = elements.intersect(ids)

  it should "load next elements using provided criteria" in {
    // given
    val criteria = LoadMoreCriteria(oid(2), PagingDirection.Right, 3)

    // when
    val result = ListSliceLoader.loadSliceUsing(criteria, elements, loadElementsFn)

    // then
    result should be(listOfObjectId(3, 4, 5))
  }

  it should "load previous elements using provided criteria" in {
    // given
    val criteria = LoadMoreCriteria(oid(6), PagingDirection.Left, 2)

    // when
    val result = ListSliceLoader.loadSliceUsing(criteria, elements, loadElementsFn)

    // then
    result should be(listOfObjectId(4, 5))
  }

  it should "load first elements when no starting ID provided" in {
    // given
    val criteria = LoadMoreCriteria.fromBeginning(2)

    // when
    val result = ListSliceLoader.loadSliceUsing(criteria, elements, loadElementsFn)

    // then
    result should be(listOfObjectId(0, 1))
  }

  it should "load empty list when starting ID doesn't exist in elements" in {
    // given
    val criteria = LoadMoreCriteria(new ObjectId, PagingDirection.Left, 2)

    // when
    val result = ListSliceLoader.loadSliceUsing(criteria, elements, loadElementsFn)

    // then
    result should be('empty)
  }

  it should "load element with surroundings" in {
    // given
    val criteria = LoadMoreCriteria(oid(4), PagingDirection.Radial, 3)

    // when
    val result = ListSliceLoader.loadSliceUsing(criteria, elements, loadElementsFn)

    // then
    result should be(listOfObjectId(1, 2, 3, 4, 5, 6, 7))
  }

  it should "load surroundings of first element" in {
    // given
    val criteria = LoadMoreCriteria(oid(0), PagingDirection.Radial, 3)

    // when
    val result = ListSliceLoader.loadSliceUsing(criteria, elements, loadElementsFn)

    // then
    result should be(listOfObjectId(0, 1, 2, 3))
  }

  it should "load surroundings of last element" in {
    // given
    val criteria = LoadMoreCriteria(oid(9), PagingDirection.Radial, 3)

    // when
    val result = ListSliceLoader.loadSliceUsing(criteria, elements, loadElementsFn)

    // then
    result should be(listOfObjectId(6, 7, 8, 9))
  }

  private def listOfObjectId(ids: Int*) = ids.map(ObjectIdTestUtils.oid(_)).toList

}
