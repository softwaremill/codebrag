package com.softwaremill.codebrag.service.user

import java.util.concurrent._

import com.softwaremill.codebrag.dao.user.SQLUserDAO
import com.softwaremill.codebrag.dao.{ObjectIdTestUtils, RequiresDb}
import com.softwaremill.codebrag.domain.PlainUserToken
import com.softwaremill.codebrag.domain.builder.UserAssembler
import com.softwaremill.codebrag.service.events.MockEventBus
import com.softwaremill.codebrag.test.{ClearSQLDataAfterTest, FlatSpecWithSQL}
import org.joda.time.DateTime
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar

class OldTokenReplacementSpec extends FlatSpecWithSQL with ClearSQLDataAfterTest with BeforeAndAfterEach with ShouldMatchers with MockitoSugar with MockEventBus {

  val userDAO = new SQLUserDAO(sqlDatabase)
  val authenticator = new UserPasswordAuthenticator(userDAO, eventBus) {
    override val scheduledExecutor = new ScheduledThreadPoolExecutor(1) {
      var task: Runnable = _
      override def schedule(command: Runnable, delay: Long, unit: TimeUnit): ScheduledFuture[_] = {
        task = command
        mock[ScheduledFuture[_]]
      }
      def runTask() = task.run()
    }
  }

  val fixtureId = ObjectIdTestUtils.oid(1234)
  val fixtureToken = PlainUserToken("oldToken", DateTime.now)
  val ActiveUser = UserAssembler.randomUser
    .withId(fixtureId)
    .withToken(fixtureToken)
    .get

  override def beforeEach() = {
    super.beforeEach()
    userDAO.add(ActiveUser)
  }

  it should "create new token if there is no old token" taggedAs RequiresDb in {
    // when
    val token = authenticator.deleteOldSoonAndCreateNewToken(ActiveUser, None)

    //then
    val Some(user) = userDAO.findById(fixtureId)
    user.tokens.map(_.token) should contain (token.hashed.token)
  }

  it should "not replace old token immediately" taggedAs RequiresDb in {
    // when
    val newToken = authenticator.deleteOldSoonAndCreateNewToken(ActiveUser, Some(fixtureToken.hashed))

    //then
    val Some(oldUser) = userDAO.findById(fixtureId)
    oldUser.tokens.map(_.token) should not contain newToken.hashed.token
    oldUser.tokens.map(_.token) should contain (fixtureToken.hashed.token)
  }
  
  it should "replace old token when the time comes" taggedAs RequiresDb in {
    // when
    val newToken = authenticator.deleteOldSoonAndCreateNewToken(ActiveUser, Some(fixtureToken.hashed))

    //then
    authenticator.scheduledExecutor.runTask()
    val Some(newUser) = userDAO.findById(fixtureId)
    newUser.tokens.map(_.token) should contain (newToken.hashed.token)
    newUser.tokens.map(_.token) should not contain fixtureToken.hashed.token
  }

}
