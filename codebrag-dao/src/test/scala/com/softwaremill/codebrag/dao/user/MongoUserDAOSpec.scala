package com.softwaremill.codebrag.dao.user

import com.softwaremill.codebrag.domain._
import org.scalatest.matchers.ShouldMatchers
import org.bson.types.ObjectId
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.test.mongo.ClearDataAfterTest
import com.softwaremill.codebrag.domain.builder.{UserAssembler, CommitInfoAssembler}
import org.joda.time.DateTime
import com.softwaremill.codebrag.domain.LastUserNotificationDispatch
import com.softwaremill.codebrag.dao.{FlatSpecWithMongo, ObjectIdTestUtils, RequiresDb}

class MongoUserDAOSpec extends MongoUserSpec with ChangeUserSettingsSpec {

  "regular user dao" should "not consider users that are flagged as internal" in {
    // given
    UserRecord.drop // clean database from preloaded uses
    val internalUser = InternalUser("codebrag")
    internalUserDAO.createIfNotExists(internalUser)

    // then
    userDAO.findAll() should be('empty)
    userDAO.findById(internalUser.id) should be('empty)
  }

  "other methods" should "add user with existing login" taggedAs (RequiresDb) in {
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

  it should "generate the id if one is not present" taggedAs (RequiresDb) in {
    // Given
    val user = User(Authentication.basic("x", "x"), "x", "y", "z", "")

    // When
    val addedUser = userDAO.add(user)

    // then
    user.id should be(null)
    addedUser.id should not be (null)
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
    val userOpt = userDAO.findByLoginOrEmail(login, "")

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
    val userOpt = userDAO.findByLoginOrEmail("", email)

    // Then
    userOpt match {
      case Some(u) => u.email should be(email.toLowerCase())
      case _ => fail("User option should be defined")
    }
  }

  it should "find commit author by email" taggedAs (RequiresDb) in {
    // Given
    val commit = CommitInfoAssembler.randomCommit.withAuthorEmail("user1@sml.com").get

    // When
    val userOpt = userDAO.findCommitAuthor(commit)

    // Then
    userOpt match {
      case Some(u) => u.email should be(commit.authorEmail)
      case _ => fail("User option should be defined")
    }
  }

  it should "find commit author by name" taggedAs (RequiresDb) in {
    // Given
    val commit = CommitInfoAssembler.randomCommit.withAuthorName("User Name 1").get

    // When
    val userOpt = userDAO.findCommitAuthor(commit)

    // Then
    userOpt match {
      case Some(u) => u.name should be(commit.authorName)
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

  it should "find user by its Id" taggedAs RequiresDb in {
    // given
    val user = User(ObjectIdTestUtils.oid(123), Authentication.basic("user", "password"), "user", "user@email.com", "12345abcde", UserSettings("avatarUrl"), None)
    userDAO.add(user)

    // when
    val foundUser = userDAO.findById(user.id)

    // then
    foundUser.get should equal(user)
  }

  it should "store notifications dates properly" taggedAs RequiresDb in {
    // given
    val commitDate = DateTime.now().minusHours(1)
    val followupDate = DateTime.now().minusMinutes(1)
    val notifications = Some(LastUserNotificationDispatch(Some(commitDate), Some(followupDate)))
    val user = User(ObjectIdTestUtils.oid(123), Authentication.basic("user", "password"), "user", "user@email.com", "12345abcde", UserSettings("avatarUrl"), notifications)
    userDAO.add(user)

    // when
    val foundUser = userDAO.findById(user.id)

    // then
    foundUser.get.notifications.get.commits.get.getMillis should equal(commitDate.getMillis)
    foundUser.get.notifications.get.followups.get.getMillis should equal(followupDate.getMillis)
  }

  "rememberNotifications" should "store dates properly" taggedAs RequiresDb in {
    // given
    val user = User(ObjectIdTestUtils.oid(123), Authentication.basic("user", "password"), "user", "user@email.com", "12345abcde", "avatarUrl")
    userDAO.add(user)
    val followupDate = DateTime.now()
    val notifications = LastUserNotificationDispatch(None, Some(followupDate))

    // when
    userDAO.rememberNotifications(user.id, notifications)

    // then
    val foundUser = userDAO.findById(user.id)
    foundUser.get.notifications.get.commits should equal(None)
    foundUser.get.notifications.get.followups.get.getMillis should equal(followupDate.getMillis)
  }

  it should "update existing dates" taggedAs RequiresDb in {
    // given
    val notifications = Some(LastUserNotificationDispatch(Some(DateTime.now().minusHours(5)), Some(DateTime.now().minusMinutes(5))))
    val user = User(ObjectIdTestUtils.oid(123), Authentication.basic("user", "password"), "user", "user@email.com", "12345abcde", UserSettings("avatarUrl"), notifications)
    userDAO.add(user)

    // when
    val commitDate = DateTime.now().minusMinutes(1)
    val followupDate = DateTime.now().minusMinutes(1)
    val newNotifications = LastUserNotificationDispatch(Some(commitDate), Some(followupDate))
    userDAO.rememberNotifications(user.id, newNotifications)

    // then
    val foundUser = userDAO.findById(user.id)
    foundUser.get.notifications.get.commits.get.getMillis should equal(commitDate.getMillis)
    foundUser.get.notifications.get.followups.get.getMillis should equal(followupDate.getMillis)
  }

}

trait MongoUserSpec extends FlatSpecWithMongo with ShouldMatchers with ClearDataAfterTest with Logging {
  val UserIdPrefix = "507f1f77bcf86cd79943901"
  var userDAO: UserDAO = _
  var internalUserDAO: MongoInternalUserDAO = _

  implicit def intSuffixToObjectId(suffix: Int): ObjectId = new ObjectId(UserIdPrefix + suffix)

  override def beforeEach() {
    super.beforeEach()
    userDAO = new MongoUserDAO
    internalUserDAO = new MongoInternalUserDAO

    for (i <- 1 to 3) {
      val login = "user" + i
      val password = "pass" + i
      val token = "token" + i
      val name = s"User Name $i"
      userDAO.add(User(i, Authentication.basic(login, password), name, s"$login@sml.com", token, UserSettings("avatarUrl"), None))
    }
  }
}

trait ChangeUserSettingsSpec extends MongoUserSpec {

  val user = UserAssembler.randomUser.withId(9).withEmailNotificationsDisabled().withWelcomeFollowupNotYetDone().get

  "changeUserSettings" should "change email notifications" taggedAs RequiresDb in {
    //given
    userDAO.add(user)

    //when
    userDAO.changeUserSettings(9, user.settings.copy(emailNotificationsEnabled = true))

    //then
    userDAO.findById(9).get.settings.emailNotificationsEnabled should equal(true)
  }

  it should "update multiple settings" taggedAs RequiresDb in {
    //given
    val newSettings = user.settings.copy(emailNotificationsEnabled = true, appTourDone = true)
    userDAO.add(user)

    //when
    userDAO.changeUserSettings(9, newSettings)

    //then
    val Some(userFound) = userDAO.findById(9)
    userFound.settings.emailNotificationsEnabled should equal(true)
    userFound.settings.appTourDone should equal(true)
  }
}