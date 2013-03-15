package com.softwaremill.codebrag.service.user

import com.softwaremill.codebrag.dao.{InMemoryUserDAO, UserDAO}
import com.softwaremill.codebrag.domain.User
import com.softwaremill.codebrag.service.schedulers.EmailSendingService
import com.softwaremill.codebrag.service.templates.{EmailContentWithSubject, EmailTemplatingEngine}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.mockito.Matchers

class UserServiceSpec extends FlatSpec with ShouldMatchers with MockitoSugar with BeforeAndAfter {
  def prepareUserDAOMock: UserDAO = {
    val dao = new InMemoryUserDAO
    dao.add(User("Admin", "admin@sml.com", "pass", "salt", "token1"))
    dao.add(User("Admin2", "admin2@sml.com", "pass", "salt", "token2"))
    dao
  }

  val emailSendingService: EmailSendingService = mock[EmailSendingService]
  val emailTemplatingEngine = mock[EmailTemplatingEngine]
  var userDAO: UserDAO = _
  var userService: UserService = _

  before {
    userDAO = prepareUserDAOMock
    userService = new UserService(userDAO, emailSendingService, emailTemplatingEngine)
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
