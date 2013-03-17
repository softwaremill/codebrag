package com.softwaremill.codebrag.service.user

import com.softwaremill.codebrag.dao.{InMemoryUserDAO, UserDAO}
import com.softwaremill.codebrag.domain.User
import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar

class UserServiceSpec extends FlatSpec with ShouldMatchers with MockitoSugar with BeforeAndAfter {
  def prepareUserDAOMock: UserDAO = {
    val dao = new InMemoryUserDAO
    dao.add(User("Admin", "admin@sml.com", "pass", "salt", "token1"))
    dao.add(User("Admin2", "admin2@sml.com", "pass", "salt", "token2"))
    dao
  }

  var userDAO: UserDAO = _
  var userService: UserService = _

  before {
    userDAO = prepareUserDAOMock
    userService = new UserService(userDAO)
  }

  // this test is silly :\
  "findByEmail" should "return user for admin@sml.pl" in {
    val userOpt = userService.findByEmail("admin@sml.com")

    userOpt.map(_.login) should be (Some("Admin"))
  }

  "findByEmail" should  "return user for uppercased ADMIN@SML.PL" in {
    val userOpt = userService.findByEmail("ADMIN@SML.COM")

    userOpt.map(_.login) should be (Some("Admin"))
  }

}
