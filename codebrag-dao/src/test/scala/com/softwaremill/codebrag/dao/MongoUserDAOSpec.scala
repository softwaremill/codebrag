package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.{Authentication, User}
import org.scalatest.matchers.ShouldMatchers
import org.bson.types.ObjectId
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.test.mongo.ClearDataAfterTest

class MongoUserDAOSpec extends FlatSpecWithMongo with ShouldMatchers with ClearDataAfterTest with Logging {

  val userIdPrefix = "507f1f77bcf86cd79943901"
  var userDAO: UserDAO = _

  implicit def intSuffixToObjectId(suffix: Int): ObjectId = new ObjectId(userIdPrefix + suffix)

  override def beforeEach() {
    super.beforeEach()
    userDAO = new MongoUserDAO

    for (i <- 1 to 3) {
      val login = "user" + i
      val password = "pass" + i
      val salt = "salt" + i
      val token = "token" + i
      val name = s"User Name $i"
      userDAO.add(User(i, Authentication.basic(login, password), name, s"$login@sml.com", token, "avatarUrl"))
    }
  }

  it should "add user with existing login" taggedAs (RequiresDb) in {
    // Given
    val login = "user1"
    val email = "anotherEmaill@sml.com"
    val authentication = Authentication.basic(login, login)
    val name = "User Name"
    val token = "token"
    val avatarUrl = "avatarUrl"

    // When
    userDAO.add(User(authentication, name, email, token, avatarUrl))

    // then
    assert(userDAO.findByLoginOrEmail(login).isDefined)
  }

  it should "add user with existing email" taggedAs (RequiresDb) in {
    // Given
    val login = "anotherUser"
    val email = "1email@sml.com"
    val authentication = Authentication.basic(login, login)
    val name = "User Name"
    val token = "token"
    val avatarUrl = "avatarUrl"

    // When
    userDAO.add(User(authentication, name, email, token, avatarUrl))

    // then
    assert(userDAO.findByLoginOrEmail(email).isDefined)
  }

  it should "find by email" taggedAs (RequiresDb) in {
    // Given
    val email = "user1@sml.com"

    // When
    val userOpt = userDAO.findByEmail(email)

    // Then
    userOpt match {
      case Some(u) => u.email should be(email)
      case _ => fail("User option should be defined")
    }
  }

  it should "not find non-existing user by email" taggedAs (RequiresDb) in {
    // Given
    val email: String = "anyEmail@sml.com"

    // When
    val userOpt = userDAO.findByEmail(email)

    // Then
    userOpt match {
      case Some(_) => fail("User option should be defined")
      case _ => // ok
    }
  }

  it should "not find non-existing user by login" taggedAs (RequiresDb) in {
    // Given
    val login = "non_existing_login"

    // When
    val userOpt = userDAO.findByLowerCasedLogin(login)

    // Then
    userOpt should be(None)
  }

  it should "not find non-existing user by login or email" taggedAs (RequiresDb) in {
    // Given
    val login = "non_existing_login"

    // When
    val userOpt = userDAO.findByLoginOrEmail(login)

    // Then
    userOpt should be(None)
  }

  it should "find by uppercased email" taggedAs (RequiresDb) in {
    // Given
    val email = "user1@sml.com".toUpperCase

    // When
    val userOpt = userDAO.findByEmail(email)

    // Then
    userOpt match {
      case Some(u) => u.email should be(email.toLowerCase)
      case _ => fail("User option should be defined")
    }
  }

  it should "find by login" taggedAs (RequiresDb) in {
    // Given
    val login = "user1"

    // When
    val userOpt = userDAO.findByLowerCasedLogin(login)

    // Then
    userOpt match {
      case Some(u) => u.authentication.username should be(login)
      case _ => fail("User option should be defined")
    }
  }


  it should "find by uppercased login" taggedAs (RequiresDb) in {
    // Given
    val login = "user1".toUpperCase

    // When
    val userOpt = userDAO.findByLowerCasedLogin(login)

    // Then
    userOpt match {
      case Some(u) => u.authentication.usernameLowerCase should be(login.toLowerCase)
      case _ => fail("User option should be defined")
    }
  }

  it should "find using login with findByLoginOrEmail" taggedAs (RequiresDb) in {
    // Given
    val login = "user1"

    // When
    val userOpt = userDAO.findByLoginOrEmail(login)

    // Then
    userOpt match {
      case Some(u) => u.authentication.username should be(login.toLowerCase())
      case _ => fail("User option should be defined")
    }
  }

  it should "find using uppercased login with findByLoginOrEmail" taggedAs (RequiresDb) in {
    // Given
    val login = "user1".toUpperCase

    // When
    val userOpt = userDAO.findByLoginOrEmail(login)

    // Then
    userOpt match {
      case Some(u) => u.authentication.username should be(login.toLowerCase())
      case _ => fail("User option should be defined")
    }
  }

  it should "find using email with findByLoginOrEmail" taggedAs (RequiresDb) in {
    // Given
    val email = "user1@sml.com"

    // When
    val userOpt = userDAO.findByLoginOrEmail(email)

    // Then
    userOpt match {
      case Some(u) => u.email should be(email.toLowerCase())
      case _ => fail("User option should be defined")
    }
  }

  it should "find using uppercased email with findByLoginOrEmail" taggedAs (RequiresDb) in {
    // Given
    val email = "user1@sml.com".toUpperCase

    // When
    val userOpt = userDAO.findByLoginOrEmail(email)

    // Then
    userOpt match {
      case Some(u) => u.email should be(email.toLowerCase())
      case _ => fail("User option should be defined")
    }
  }

  it should "find by token" taggedAs (RequiresDb) in {
    // Given
    val token = "token1"

    // When
    val userOpt = userDAO.findByToken(token)

    // Then
    userOpt match {
      case Some(u) => u.token should be(token)
      case _ => fail("User option should be defined")
    }
  }

  it should "replace existing authentication" taggedAs (RequiresDb) in {
    val auth = Authentication.github("u", "at")

    userDAO.changeAuthentication(1, auth)

    userDAO.findByEmail("user1@sml.com") match {
      case Some(u) => u.authentication should equal(auth)
      case None => fail("Authentication didn't change")
    }

  }

  it should "find user by its Id" taggedAs (RequiresDb) in {
    // given
    val user = User(ObjectIdTestUtils.oid(123), Authentication.basic("user", "password"), "user", "user@email.com", "12345abcde","avatarUrl")
    userDAO.add(user)

    // when
    val foundUser = userDAO.findById(user.id)

    // then
    foundUser.get should equal(user)
  }

}