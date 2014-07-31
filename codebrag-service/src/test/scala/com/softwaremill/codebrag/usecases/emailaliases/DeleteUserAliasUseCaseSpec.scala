package com.softwaremill.codebrag.usecases.emailaliases

import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.builder.UserAssembler
import com.softwaremill.codebrag.domain.UserAlias
import org.bson.types.ObjectId
import org.mockito.Mockito._
import com.softwaremill.codebrag.dao.user.UserAliasDAO

class DeleteUserAliasUseCaseSpec extends FlatSpec with MockitoSugar with ShouldMatchers with BeforeAndAfterEach {

  var useCase: DeleteUserAliasUseCase = _

  val userAliasDao = mock[UserAliasDAO]

  val email = "alias@codebrag.com"
  val Bob = UserAssembler.randomUser.withEmail(email).get
  val BobAlias = UserAlias(new ObjectId, Bob.id, email)
  val OtherUserAlias = UserAlias(new ObjectId, new ObjectId, email)

  override def beforeEach() {
    reset(userAliasDao)
    useCase = new DeleteUserAliasUseCase(userAliasDao)
  }

  it should "not remove alias if it doesn't belong to current user" in {
    // given
    when(userAliasDao.findById(OtherUserAlias.id)).thenReturn(Some(OtherUserAlias))

    // when
    val Left(result) = useCase.execute(Bob.id, OtherUserAlias.id)

    // then
    result.flatMap(_._2) should be(List("You don't have such alias defined"))
  }

  it should "not remove alias when alias not found :)" in {
    // given
    when(userAliasDao.findById(OtherUserAlias.id)).thenReturn(None)

    // when
    val Left(result) = useCase.execute(Bob.id, OtherUserAlias.id)

    // then
    result.flatMap(_._2) should be(List("You don't have such alias defined"))
  }

  it should "remove alias if it belongs to current user" in {
    // given
    when(userAliasDao.findById(BobAlias.id)).thenReturn(Some(BobAlias))

    // when
    val result = useCase.execute(Bob.id, BobAlias.id)

    // then
    result should be('right)
  }
}
