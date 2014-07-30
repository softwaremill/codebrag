package com.softwaremill.codebrag.usecases

import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.dao.user.{UserDAO, UserAliasDAO}
import org.mockito.Mockito._
import com.softwaremill.codebrag.domain.builder.UserAssembler
import com.softwaremill.codebrag.domain.UserAlias
import org.bson.types.ObjectId

class AddUserAliasUseCaseSpec extends FlatSpec with MockitoSugar with ShouldMatchers with BeforeAndAfterEach {

  var useCase: AddUserAliasUseCase = _

  val userAliasDao = mock[UserAliasDAO]
  val userDao = mock[UserDAO]

  val email = "alias@codebrag.com"
  val Bob = UserAssembler.randomUser.withEmail(email).get
  val BobAlias = UserAlias(new ObjectId, Bob.id, email)
  val OtherUserAlias = UserAlias(new ObjectId, new ObjectId, email)

  override def beforeEach() {
    reset(userAliasDao, userDao)
    useCase = new AddUserAliasUseCase(userAliasDao, userDao)
  }

  it should "reject invalid email" in {
    // given
    val invalidEmail = "invalid@email@com"

    // when
    val Left(result) = useCase.execute(Bob.id, invalidEmail)

    // then
    result.flatMap(_._2) should be(List("Invalid email provided"))
  }

  it should "reject when given email exists as primary for any user" in {
    // given
    when(userDao.findByEmail(email)).thenReturn(Some(Bob))
    when(userAliasDao.findByAlias(email)).thenReturn(None)

    // when
    val Left(result) = useCase.execute(Bob.id, email)

    // then
    result.flatMap(_._2) should be(List("This email is already defined as primary"))
  }

  it should "reject when given email exists as alias for this user" in {
    // given
    when(userDao.findByEmail(email)).thenReturn(None)
    when(userAliasDao.findByAlias(email)).thenReturn(Some(BobAlias))

    // when
    val Left(result) = useCase.execute(Bob.id, email)

    // then
    result.flatMap(_._2) should be(List("You have such alias already defined"))
  }

  it should "reject when given email exists as alias for other user" in {
    // given
    when(userDao.findByEmail(email)).thenReturn(None)
    when(userAliasDao.findByAlias(email)).thenReturn(Some(OtherUserAlias))

    // when
    val Left(result) = useCase.execute(Bob.id, email)

    // then
    result.flatMap(_._2) should be(List("Such alias is already defined for other user"))
  }

  it should "create user alias when all is ok" in {
    // given
    when(userDao.findByEmail(email)).thenReturn(None)
    when(userAliasDao.findByAlias(email)).thenReturn(None)

    // when
    val Right(result) = useCase.execute(Bob.id, email)

    // then
    result.userId should be(Bob.id)
    result.alias should be(email)
  }
}
