package com.softwaremill.codebrag.usecases.branches

import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.common.ClockSpec
import com.softwaremill.codebrag.dao.branch.WatchedBranchesDao
import com.softwaremill.codebrag.domain.builder.UserAssembler
import org.mockito.Mockito
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.UserWatchedBranch

class RemoveBranchFromObservedSpec extends FlatSpec with MockitoSugar with ShouldMatchers with BeforeAndAfterEach with ClockSpec {

  val dao = mock[WatchedBranchesDao]
  val useCase = new StopWatchingBranch(dao)

  val Bob = UserAssembler.randomUser.get

  override def beforeEach() {
    Mockito.reset(dao)
  }

  it should "not attempt to remove branch from observed if user is not observing this branch" in {
    // given
    val form = WatchedBranchForm("codebrag", "master")
    Mockito.when(dao.findAll(Bob.id)).thenReturn(Set.empty[UserWatchedBranch])

    // when
    val Left(result) = useCase.execute(Bob.id, form)

    // then
    result.flatMap(_._2) should be(Seq("You're not watching this branch"))
  }

  it should "remove branch from observed for user" in {
    // given
    val observed = UserWatchedBranch(new ObjectId, Bob.id, "codebrag", "master")
    val form = WatchedBranchForm("codebrag", "master")
    Mockito.when(dao.findAll(Bob.id)).thenReturn(Set(observed))

    // when
    val result = useCase.execute(Bob.id, form)

    // then
    result should be('right)
  }

}
