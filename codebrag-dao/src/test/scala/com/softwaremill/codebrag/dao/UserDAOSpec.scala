package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.User
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FlatSpec, BeforeAndAfterAll}
import org.bson.types.ObjectId
import com.typesafe.scalalogging.slf4j.Logging

class MongoUserDAOSpec extends FlatSpecWithMongo with UserDAOSpec {
  behavior of "MongoUserDAO"

  def createDAO = new MongoUserDAO()
}

class InMemoryUserDAOSpec extends FlatSpecWithMongo with UserDAOSpec {
  behavior of "InMemoryUserDAO"

  def createDAO = new InMemoryUserDAO()
}

trait UserDAOSpec extends FlatSpec with ShouldMatchers with BeforeAndAfterAll with Logging {
  def createDAO: UserDAO

  val userIdPrefix = "507f1f77bcf86cd79943901"
  var userDAO: UserDAO = null
  implicit def intSuffixToObjectId(suffix: Int): ObjectId = new ObjectId(userIdPrefix + suffix)

  override def beforeAll() {
    super.beforeAll()
    userDAO = createDAO

    for (i <- 1 to 3) {
      val login = "user" + i
      val password = "pass" + i
      val salt = "salt" + i
      val token = "token" + i
      userDAO.add(User(i, login, login.toLowerCase, i + "email@sml.com", password, salt, token))
    }
  }

  it should "add user with existing login" in {
    // Given
    val login = "user1"
    val email = "anotherEmaill@sml.com"

    // When
    userDAO.add(User(login, email, "pass", "salt", "token"))

    // then
    assert(userDAO.findByLoginOrEmail(login).isDefined)
  }

  it should "add user with existing email" in {
    // Given
    val login = "anotherUser"
    val email = "1email@sml.com"

    // When
    userDAO.add(User(login, email, "pass", "salt", "token"))

    // then
    assert(userDAO.findByLoginOrEmail(email).isDefined)
  }

  it should "find by email" in {
    // Given
    val email: String = "1email@sml.com"

    // When
    val userOpt: Option[User] = userDAO.findByEmail(email)

    // Then
    userOpt match {
      case Some(u) => u.email should be (email)
      case _ => fail("User option should be defined")
    }
  }

  it should "not find non-existing user by email" in {
    // Given
    val email: String = "anyEmail@sml.com"

    // When
    val userOpt: Option[User] = userDAO.findByEmail(email)

    // Then
    userOpt match {
      case Some(_) => fail("User option should be defined")
      case _ => // ok
    }
  }

  it should "find non-existing user by login" in {
    // Given
    val login: String = "non_existing_login"

    // When
    val userOpt: Option[User] = userDAO.findByLowerCasedLogin(login)

    // Then
    userOpt match {
      case Some(u) => u.login should be (login)
      case _ => fail("User option should be defined")
    }
  }

  it should "find non-existing user by login or email" in {
    // Given
    val login: String = "non_existing_login"

    // When
    val userOpt: Option[User] = userDAO.findByLoginOrEmail(login)

    // Then
    userOpt match {
      case Some(u) => u.login should be (login)
      case _ => fail("User option should be defined")
    }
  }

  it should "find by uppercased email" in {
    // Given
    val email: String = "1email@sml.com".toUpperCase

    // When
    val userOpt: Option[User] = userDAO.findByEmail(email)

    // Then
    userOpt match {
      case Some(u) => u.email should be (email.toLowerCase)
      case _ => fail("User option should be defined")
    }
  }

  it should "find by login" in {
    // Given
    val login: String = "user1"

    // When
    val userOpt: Option[User] = userDAO.findByLowerCasedLogin(login)

    // Then
    userOpt match {
      case Some(u) => u.login should be (login)
      case _ => fail("User option should be defined")
    }
  }


  it should "find by uppercased login" in {
    // Given
    val login: String = "user1".toUpperCase

    // When
    val userOpt: Option[User] = userDAO.findByLowerCasedLogin(login)

    // Then
    userOpt match {
      case Some(u) => u.login should be (login.toLowerCase())
      case _ => fail("User option should be defined")
    }
  }

  it should "find using login with findByLoginOrEmail" in {
    // Given
    val login: String = "user1"

    // When
    val userOpt: Option[User] = userDAO.findByLoginOrEmail(login)

    // Then
    userOpt match {
      case Some(u) => u.login should be (login.toLowerCase())
      case _ => fail("User option should be defined")
    }
  }

  it should "find using uppercased login with findByLoginOrEmail" in {
    // Given
    val login: String = "user1".toUpperCase

    // When
    val userOpt: Option[User] = userDAO.findByLoginOrEmail(login)

    // Then
    userOpt match {
      case Some(u) => u.login should be (login.toLowerCase())
      case _ => fail("User option should be defined")
    }
  }

  it should "find using email with findByLoginOrEmail" in {
    // Given
    val email: String = "1email@sml.com"

    // When
    val userOpt: Option[User] = userDAO.findByLoginOrEmail(email)

    // Then
    userOpt match {
      case Some(u) => u.email should be (email.toLowerCase())
      case _ => fail("User option should be defined")
    }
  }

  it should "find using uppercased email with findByLoginOrEmail" in {
    // Given
    val email: String = "1email@sml.com".toUpperCase

    // When
    val userOpt: Option[User] = userDAO.findByLoginOrEmail(email)

    // Then
    userOpt match {
      case Some(u) => u.email should be (email.toLowerCase())
      case _ => fail("User option should be defined")
    }
  }

  it should "find by token" in {
    // Given
    val token = "token1"

    // When
    val userOpt: Option[User] = userDAO.findByToken(token)

    // Then
    userOpt match {
      case Some(u) => u.token should be (token)
      case _ => fail("User option should be defined")
    }
  }

}
