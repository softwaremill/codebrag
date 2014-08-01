package com.softwaremill.codebrag.usecases.branches

import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.common.ClockSpec
import com.softwaremill.codebrag.dao.branch.WatchedBranchesDao
import com.softwaremill.codebrag.licence.{LicenceExpiredException, LicenceService}
import org.mockito.Mockito
import com.softwaremill.codebrag.domain.builder.UserAssembler
import org.bson.types.ObjectId
import org.mockito.Mockito._
import com.softwaremill.codebrag.domain.UserWatchedBranch

class AddBranchToObservedSpec extends FlatSpec with MockitoSugar with ShouldMatchers with BeforeAndAfterEach with ClockSpec {

  val dao = mock[WatchedBranchesDao]
  val licenceService = mock[LicenceService]
  val useCase = new StartWatchingBranch(dao, licenceService)

  val Bob = UserAssembler.randomUser.get

  override def beforeEach() {
    Mockito.reset(dao, licenceService)
  }

  it should "add branch to observed for given user if not exists yet" in {
    // given
    Mockito.when(dao.findAll(Bob.id)).thenReturn(Set.empty[UserWatchedBranch])
    val form = WatchedBranchForm("codebrag", "master")

    // when
    val observable = useCase.execute(Bob.id, form)

    // then
    observable should be('right)
  }

  it should "not add branch to observed if it is already being observed by user" in {
    // given
    val alreadyObservedBranches = Set(UserWatchedBranch(new ObjectId, Bob.id, "codebrag", "master"))
    Mockito.when(dao.findAll(Bob.id)).thenReturn(alreadyObservedBranches)
    val form = WatchedBranchForm("codebrag", "master")

    // when
    val Left(observable) = useCase.execute(Bob.id, form)

    // then
    observable.flatMap(_._2) should be(Seq("You're already watching this branch"))
  }

  it should "halt if licence is not valid" in {
    // given
    when(licenceService.interruptIfLicenceExpired()).thenThrow(new LicenceExpiredException)
    val form = WatchedBranchForm("codebrag", "master")

    // when
    intercept[Exception] {
      useCase.execute(Bob.id, form)
    }

    // then exception should be thrown
    Mockito.verifyZeroInteractions(dao)
  }

}
