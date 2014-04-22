package com.softwaremill.codebrag.activities

import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import org.mockito.Mockito._
import com.softwaremill.codebrag.service.comments.{UserReactionService, LikeValidator}
import com.softwaremill.codebrag.service.data.UserJson
import com.softwaremill.codebrag.domain.builder.UserAssembler
import com.softwaremill.codebrag.dao.ObjectIdTestUtils
import org.bson.types.ObjectId
import com.softwaremill.codebrag.licence.LicenceService

class UnlikeUseCaseSpec extends FlatSpec with MockitoSugar with ShouldMatchers with BeforeAndAfterEach {

  var likeValidator: LikeValidator = _
  var userReactionService: UserReactionService = _
  var licenceService: LicenceService = _
  var unlikeUseCase: UnlikeUseCase = _

  override def beforeEach() {
    likeValidator = mock[LikeValidator]
    userReactionService = mock[UserReactionService]
    licenceService = mock[LicenceService]
    unlikeUseCase = new UnlikeUseCase(likeValidator, userReactionService, licenceService)
  }

  it should "tell when unlike cannot be done due to validation rules" in {
    // given
    val user = UserJson(UserAssembler.randomUser.get)
    val likeIdToRemove = ObjectIdTestUtils.oid(100)
    when(likeValidator.canUserDoUnlike(new ObjectId(user.id), likeIdToRemove)).thenReturn(Left("err"))

    // when
    val result = unlikeUseCase.execute(user, likeIdToRemove)

    // then
    result.left.get should be("err")
  }

  it should "tell when unlike can be done" in {
    // given
    val user = UserJson(UserAssembler.randomUser.get)
    val likeIdToRemove = ObjectIdTestUtils.oid(100)
    when(likeValidator.canUserDoUnlike(new ObjectId(user.id), likeIdToRemove)).thenReturn(Right())

    // when
    val result = unlikeUseCase.execute(user, likeIdToRemove)

    // then
    result.isRight should be(true)
  }

  it should "initiate like removal when like can be removed" in {
    // given
    val user = UserJson(UserAssembler.randomUser.get)
    val likeIdToRemove = ObjectIdTestUtils.oid(100)
    when(likeValidator.canUserDoUnlike(new ObjectId(user.id), likeIdToRemove)).thenReturn(Right())

    // when
    unlikeUseCase.execute(user, likeIdToRemove)

    // then
    verify(userReactionService).removeLike(likeIdToRemove)
  }

  it should "not remove like and return err when validation rules are not met" in {
    // given
    val user = UserJson(UserAssembler.randomUser.get)
    val likeIdToRemove = ObjectIdTestUtils.oid(100)
    when(likeValidator.canUserDoUnlike(new ObjectId(user.id), likeIdToRemove)).thenReturn(Left("err"))

    // when
    val result = unlikeUseCase.execute(user, likeIdToRemove)

    // then
    verifyZeroInteractions(userReactionService)
    result.left.get should be("err")
  }

}
