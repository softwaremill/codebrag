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

  it should "load all users" in {
    userDAO.loadAll should have size (3)
  }

  it should "count all users" in {
    userDAO.countItems() should be (3)
  }

  it should "add new user" in {
    // Given
    val numberOfUsersBefore = userDAO.countItems()
    val login = "newuser"
    val email = "newemail@sml.com"

    // When
    userDAO.add(User(login, email, "pass", "salt", "token"))

    // Then
    (userDAO.countItems() - numberOfUsersBefore) should be (1)
  }


  it should "throw exception when trying to add user with existing login" in {
    // Given
    val login = "newuser"
    val email = "anotherEmaill@sml.com"

    // When & then
    intercept[Exception] {
      userDAO.add(User(login, email, "pass", "salt", "token"))
    }
  }

  it should "throw exception when trying to add user with existing email" in {
    // Given
    val login = "anotherUser"
    val email = "newemail@sml.com"

    // When
    intercept[Exception] {
      userDAO.add(User(login, email, "pass", "salt", "token"))
    }
  }

  it should "remove user" in {
    // Given
    val numberOfUsersBefore = userDAO.countItems()
    val userOpt: Option[User] = userDAO.findByLoginOrEmail("newuser")

    // When
    userOpt.foreach(u => userDAO.remove(u.id.toString))

    // Then
    (userDAO.countItems() - numberOfUsersBefore) should be (-1)
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

  it should "find users by identifiers" in {
    // Given
    val ids: List[ObjectId] = List(1, 2, 2);

    // When
    val users = userDAO.findForIdentifiers(ids)

    // Then
    users.map(user => user.login) should be(List("user1", "user2"))
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
