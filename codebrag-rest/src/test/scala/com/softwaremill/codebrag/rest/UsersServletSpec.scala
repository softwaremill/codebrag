package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.user.UserService
import com.softwaremill.codebrag.dao.InMemoryUserDAO
import com.softwaremill.codebrag.domain.User
import com.softwaremill.codebrag.CodebragServletSpec
import org.json4s.JsonDSL._
import com.softwaremill.codebrag.service.schedulers.DummyEmailSendingService
import com.softwaremill.codebrag.service.templates.EmailTemplatingEngine
import org.mockito.Mockito._

class UsersServletSpec extends CodebragServletSpec {
  var servlet: UsersServlet = _

  def onServletWithMocks(testToExecute: (UserService) => Unit) = {
    val dao = new InMemoryUserDAO
    dao.add(User("Admin", "admin@sml.com", "pass", "salt", "token1"))
    dao.add(User("Admin2", "admin2@sml.com", "pass", "salt", "token2"))

    val userService = spy(new UserService(dao, new DummyEmailSendingService(), new EmailTemplatingEngine))

    servlet = new UsersServlet(userService)
    addServlet(servlet, "/*")

    testToExecute(userService)
  }

}
