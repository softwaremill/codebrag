package com.softwaremill.codebrag.service.user

import java.util.concurrent._

import com.softwaremill.codebrag.dao.user.SQLUserDAO
import com.softwaremill.codebrag.dao.{ObjectIdTestUtils, RequiresDb}
import com.softwaremill.codebrag.domain.UserToken
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
  val fixtureToken = UserToken("oldToken", DateTime.now)
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
    val UserToken(token, _) = authenticator.deleteOldSoonAndCreateNewToken(ActiveUser, None)

    //then
    val Some(user) = userDAO.findById(fixtureId)
    user.tokens.map(_.token) should contain (token)
  }

  it should "not replace old token immediately" taggedAs RequiresDb in {
    // when
    val UserToken(newToken, _) = authenticator.deleteOldSoonAndCreateNewToken(ActiveUser, Some(fixtureToken))

    //then
    val Some(oldUser) = userDAO.findById(fixtureId)
    oldUser.tokens.map(_.token) should not contain newToken
    oldUser.tokens.map(_.token) should contain ("oldToken")
  }
  
  it should "replace old token when the time comes" taggedAs RequiresDb in {
    // when
    val UserToken(newToken, _) = authenticator.deleteOldSoonAndCreateNewToken(ActiveUser, Some(fixtureToken))

    //then
    authenticator.scheduledExecutor.runTask()
    val Some(newUser) = userDAO.findById(fixtureId)
    newUser.tokens.map(_.token) should contain (newToken)
    newUser.tokens.map(_.token) should not contain "oldToken"
  }

}
