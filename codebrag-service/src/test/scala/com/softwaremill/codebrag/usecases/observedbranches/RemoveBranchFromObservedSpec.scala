package com.softwaremill.codebrag.usecases.observedbranches

import org.scalatest.{BeforeAndAfterEach, FlatSpec, FunSuite}
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.common.ClockSpec
import com.softwaremill.codebrag.dao.observedbranch.UserObservedBranchDAO
import com.softwaremill.codebrag.licence.{LicenceExpiredException, LicenceService}
import com.softwaremill.codebrag.domain.builder.UserAssembler
import org.mockito.Mockito
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.UserObservedBranch

class RemoveBranchFromObservedSpec extends FlatSpec with MockitoSugar with ShouldMatchers with BeforeAndAfterEach with ClockSpec {

  val dao = mock[UserObservedBranchDAO]
  val licenceService = mock[LicenceService]
  val useCase = new RemoveBranchFromObserved(dao, licenceService)

  val Bob = UserAssembler.randomUser.get

  override def beforeEach() {
    Mockito.reset(dao, licenceService)
  }

  it should "throw excepion if licence is expired" in {
    // given
    Mockito.when(licenceService.interruptIfLicenceExpired()).thenThrow(new LicenceExpiredException())
    val toRemove = new ObjectId

    // when
    intercept[Exception] {
      useCase.execute(Bob.id, toRemove)
    }

    // then exception should be thrown
    Mockito.verifyZeroInteractions(dao)
  }

  it should "not attempt to remove branch from observed if user is not observing this branch" in {
    // given
    Mockito.when(dao.findAll(Bob.id)).thenReturn(Set.empty[UserObservedBranch])

    // when
    val toRemove = new ObjectId
    val Left(result) = useCase.execute(Bob.id, toRemove)

    // then
    result.flatMap(_._2) should be(Seq("You're not watching this branch"))
  }

  it should "remove branch from observed for user" in {
    // given
    val observed = UserObservedBranch(new ObjectId, Bob.id, "codebrag", "master")
    Mockito.when(dao.findAll(Bob.id)).thenReturn(Set(observed))

    // when
    val result = useCase.execute(Bob.id, observed.id)

    // then
    result should be('right)
  }

}
