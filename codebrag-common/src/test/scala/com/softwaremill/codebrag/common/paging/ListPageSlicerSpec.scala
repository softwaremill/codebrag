package com.softwaremill.codebrag.common.paging

import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import PagingCriteria.Direction

class ListPageSlicerSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {

  val elements = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)

  it should "load next elements using provided criteria" in {
    // given
    val criteria = PagingCriteria(2, Direction.Right, 3)

    // when
    val page = criteria.extractPageFrom(elements)

    // then
    page.items should be(listOf(3, 4, 5))
    page.beforeCount should be(3)
    page.afterCount should be(4)
  }

  it should "load previous elements using provided criteria" in {
    // given
    val criteria = PagingCriteria(6, Direction.Left, 2)

    // when
    val page = criteria.extractPageFrom(elements)

    // then
    page.items should be(listOf(4, 5))
  }

  it should "load first elements when no starting ID provided" in {
    // given
    val criteria = PagingCriteria.fromBeginning[Int](2)

    // when
    val page = criteria.extractPageFrom(elements)

    // then
    page.items should be(listOf(0, 1))
  }

  it should "load empty list when starting ID doesn't exist in elements" in {
    // given
    val criteria = PagingCriteria(100, Direction.Left, 2)

    // when
    val page = criteria.extractPageFrom(elements)

    // then
    page.items should be('empty)
  }

  it should "load element with surroundings" in {
    // given
    val criteria = PagingCriteria(4, Direction.Radial, 3)

    // when
    val page = criteria.extractPageFrom(elements)

    // then
    page.items should be(listOf(1, 2, 3, 4, 5, 6, 7))
  }

  it should "load surroundings of first element" in {
    // given
    val criteria = PagingCriteria(0, Direction.Radial, 3)

    // when
    val page = criteria.extractPageFrom(elements)

    // then
    page.items should be(listOf(0, 1, 2, 3))
  }

  it should "load surroundings of last element" in {
    // given
    val criteria = PagingCriteria(9, Direction.Radial, 3)

    // when
    val page = criteria.extractPageFrom(elements)

    // then
    page.items should be(listOf(6, 7, 8, 9))
  }

  it should "calculate count of next and previous elements for page" in {
    // given
    val criteria = PagingCriteria(2, Direction.Right, 3)

    // when
    val page = criteria.extractPageFrom(elements)

    // then
    page.beforeCount should be(3)
    page.afterCount should be(4)
  }

  it should "return count of next/prev elements as 0 when page is at the end/beginning of list" in {
    // given
    val startCriteria = PagingCriteria.fromBeginning[Int](4)
    val endCriteria = PagingCriteria.fromEnd[Int](4)

    // when
    val startingPage = startCriteria.extractPageFrom(elements)
    val endingPage = endCriteria.extractPageFrom(elements)

    // then
    endingPage.beforeCount should be(6)
    endingPage.afterCount should be(0)
    startingPage.beforeCount should be(0)
    startingPage.afterCount should be(6)
  }

  private def listOf(ids: Int*) = ids.toList

}
