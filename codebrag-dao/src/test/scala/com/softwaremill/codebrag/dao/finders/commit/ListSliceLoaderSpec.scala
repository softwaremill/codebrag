package com.softwaremill.codebrag.dao.finders.commit

import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.common.{SurroundingsCriteria, PagingCriteria}
import com.softwaremill.codebrag.dao.ObjectIdTestUtils._
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.ObjectIdTestUtils

class ListSliceLoaderSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {

  val elements = listOfObjectId(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)

  def loadElementsFn(ids: List[ObjectId]) = elements.intersect(ids)

  it should "load next elements using provided criteria" in {
    // given
    val criteria = PagingCriteria(None, Some(oid(2)), 3)

    // when
    val result = ListSliceLoader.loadSliceUsing(criteria, elements, loadElementsFn)

    // then
    result should be(listOfObjectId(3, 4, 5))
  }

  it should "load previous elements using provided criteria" in {
    // given
    val criteria = PagingCriteria(Some(oid(6)), None, 2)

    // when
    val result = ListSliceLoader.loadSliceUsing(criteria, elements, loadElementsFn)

    // then
    result should be(listOfObjectId(4, 5))
  }

  it should "load first elements when no starting ID provided" in {
    // given
    val criteria = PagingCriteria(None, None, 2)

    // when
    val result = ListSliceLoader.loadSliceUsing(criteria, elements, loadElementsFn)

    // then
    result should be(listOfObjectId(0, 1))
  }

  it should "load empty list when starting ID doesn't exist in elements" in {
    // given
    val criteria = PagingCriteria(Some(new ObjectId), None, 2)

    // when
    val result = ListSliceLoader.loadSliceUsing(criteria, elements, loadElementsFn)

    // then
    result should be('empty)
  }

  it should "load element with surroundings" in {
    // given
    val criteria = SurroundingsCriteria(oid(4), 3)

    // when
    val result = ListSliceLoader.loadSurroundingSliceUsing(criteria, elements, loadElementsFn)

    // then
    result should be(listOfObjectId(1, 2, 3, 4, 5, 6, 7))
  }

  it should "load surroundings of first element" in {
    // given
    val criteria = SurroundingsCriteria(oid(0), 3)

    // when
    val result = ListSliceLoader.loadSurroundingSliceUsing(criteria, elements, loadElementsFn)

    // then
    result should be(listOfObjectId(0, 1, 2, 3))
  }

  it should "load surroundings of last element" in {
    // given
    val criteria = SurroundingsCriteria(oid(9), 3)

    // when
    val result = ListSliceLoader.loadSurroundingSliceUsing(criteria, elements, loadElementsFn)

    // then
    result should be(listOfObjectId(6, 7, 8, 9))
  }

  private def listOfObjectId(ids: Int*) = ids.map(ObjectIdTestUtils.oid(_)).toList

}
