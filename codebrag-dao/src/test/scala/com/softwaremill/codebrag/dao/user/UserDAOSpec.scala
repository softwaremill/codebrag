package com.softwaremill.codebrag.dao.user

import com.softwaremill.codebrag.domain._
import org.scalatest.matchers.ShouldMatchers
import org.bson.types.ObjectId
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.domain.builder.{UserAssembler, CommitInfoAssembler}
import org.joda.time.DateTime
import com.softwaremill.codebrag.domain.LastUserNotificationDispatch
import com.softwaremill.codebrag.dao.{ObjectIdTestUtils, RequiresDb}
import org.scalatest.BeforeAndAfterEach
import com.softwaremill.codebrag.test.{FlatSpecWithSQL, ClearSQLDataAfterTest}
import com.softwaremill.codebrag.common.ClockSpec

class SQLUserDAOSpec extends FlatSpecWithSQL with ClearSQLDataAfterTest with BeforeAndAfterEach with ShouldMatchers with Logging with ClockSpec {

  val userDAO = new SQLUserDAO(sqlDatabase)
  val internalUserDAO = new SQLInternalUserDAO(sqlDatabase)

  val UserIdPrefix = "507f1f77bcf86cd79943901"

  val CreatedUsersSize = 3

  implicit def intSuffixToObjectId(suffix: Int): ObjectId = new ObjectId(UserIdPrefix + suffix)

  override def beforeEach() {
    super.beforeEach()

    for (i <- 1 to CreatedUsersSize) {
      val login = "user" + i
      val password = "pass" + i
      val token = "token" + i
      val name = s"User Name $i"
      val user = UserAssembler.randomUser.withId(i).withBasicAuth(login, password).withFullName(name).withEmail(s"$login@sml.com").withToken(token).get
      userDAO.add(user)
    }
  }

  "regular user dao" should "not consider users that are flagged as internal" in {
    // given
    val internalUser = InternalUser("codebrag")
    internalUserDAO.createIfNotExists(internalUser)

    // then
    userDAO.findAll().map(_.name) should not contain ("codebrag")
    userDAO.findById(internalUser.id) should be('empty)
  }

  it should "add user with admin flag (false by default)" taggedAs(RequiresDb) in {
    // given
    val bobUser= UserAssembler.randomUser.get
    val johnAdmin = UserAssembler.randomUser.get.makeAdmin

    // when
    userDAO.add(bobUser)
    userDAO.add(johnAdmin)

    // then
    val Some(bob) = userDAO.findById(bobUser.id)
    bob.admin should be(false)
    val Some(john) = userDAO.findById(johnAdmin.id)
    john.admin should be(true)

  }

  it should "add user with existing login" taggedAs (RequiresDb) in {
    // Given
    val login = "user1"
    val user = UserAssembler.randomUser.withBasicAuth(login, "pass").withEmail("anotheremail@sml.com").get

    // When
    userDAO.add(user)

    // then
    assert(userDAO.findByLoginOrEmail(login).isDefined)
  }

  it should "add user with email aliases" taggedAs (RequiresDb) in {
    // given
    val userId = new ObjectId
    val userAliases = UserAliases(Set(UserAlias(userId, "one@codebrag.com"), UserAlias(userId, "two@codebrag.com")))
    val user = UserAssembler.randomUser.withId(userId).get.copy(aliases = userAliases)

    // when
    userDAO.add(user)

    // then
    val result = userDAO.findById(userId)
    result.get.aliases should be(userAliases)
  }

  it should "count all regular users in db" taggedAs (RequiresDb) in {
    // given
    internalUserDAO.createIfNotExists(InternalUser("codebrag"))

    // when
    val count = userDAO.countAll()

    // then
    count should be(CreatedUsersSize)
  }

  it should "add user with existing email" taggedAs (RequiresDb) in {
    // Given
    val user = UserAssembler.randomUser.withBasicAuth("anotherUser", "pass").withEmail("1email@sml.com").withToken("token").get

    // When
    userDAO.add(user)

    // then
    assert(userDAO.findByLoginOrEmail("1email@sml.com").isDefined)
  }

  it should "find by email" taggedAs (RequiresDb) in {
    // Given
    val email = "user1@sml.com"

    // When
    val userOpt = userDAO.findByEmail(email)

    // Then
    userOpt match {
      case Some(u) => u.emailLowerCase should be(email)
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
      case Some(u) => u.emailLowerCase should be(email.toLowerCase)
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
      case Some(u) => u.emailLowerCase should be(email.toLowerCase())
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
      case Some(u) => u.emailLowerCase should be(commit.authorEmail)
      case _ => fail("User option should be defined")
    }
  }

  it should "find commit author by aliased email" taggedAs (RequiresDb) in {
    // Given
    val userId = new ObjectId
    val userAliases = UserAliases(Set(UserAlias(userId, "aliased_email@codebrag.com")))
    val userWithAlias = UserAssembler.randomUser.withId(userId).get.copy(aliases = userAliases)
    userDAO.add(userWithAlias)
    val commit = CommitInfoAssembler.randomCommit.withAuthorEmail("aliased_email@codebrag.com").get

    // When
    val Some(found) = userDAO.findCommitAuthor(commit)

    // Then
    found should be(userWithAlias)
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
      case Some(u) => u.emailLowerCase should be(email.toLowerCase())
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
    val user = UserAssembler.randomUser.get
    userDAO.add(user)

    // when
    val foundUser = userDAO.findById(user.id)

    // then
    foundUser.get should equal(user)
  }

  it should "change user details" taggedAs RequiresDb in {
    // given
    val user = UserAssembler.randomUser.withBasicAuth("user", "pass").withAdmin(set = false).withActive(set = false).get
    userDAO.add(user)
    val newAuth = Authentication.basic(user.authentication.username, "newpass")

    // when
    val modifiedUser = user.copy(authentication = newAuth, admin = true, active = true)
    userDAO.modifyUser(modifiedUser)
    val Some(savedUser) = userDAO.findById(user.id)

    // then
    savedUser should be(modifiedUser)
  }

  it should "store notifications dates properly" taggedAs RequiresDb in {
    // given
    val commitDate = DateTime.now().minusHours(1)
    val followupDate = DateTime.now().minusMinutes(1)
    val notifications = LastUserNotificationDispatch(Some(commitDate), Some(followupDate))
    val user = UserAssembler.randomUser.withNotificationsDispatch(notifications).get
    userDAO.add(user)

    // when
    val foundUser = userDAO.findById(user.id)

    // then
    foundUser.get.notifications.commits.get.getMillis should equal(commitDate.getMillis)
    foundUser.get.notifications.followups.get.getMillis should equal(followupDate.getMillis)
  }
 it should "deleta a user " taggedAs RequiresDb in {
    // given
    val user = UserAssembler.randomUser.withBasicAuth("user", "pass").withAdmin(set = false).withActive(set = false).get
    userDAO.add(user)
    val newAuth = Authentication.basic(user.authentication.username, "newpass")

    // when
    val modifiedUser = user.copy(authentication = newAuth, admin = true, active = true)
    userDAO.delete(modifiedUser.id)
    val deletedUser  = userDAO.findById(user.id)

    // then
    assert(deletedUser === None , "Deletion was attempted but found  " + deletedUser)
  }
  it should "deleta a user and should not show in the list  " taggedAs RequiresDb in {
    // given
    val user = UserAssembler.randomUser.withBasicAuth("user", "pass").withAdmin(set = false).withActive(set = false).get
    userDAO.add(user)
    val newAuth = Authentication.basic(user.authentication.username, "newpass")
    
    // when
    val tobeDeletedUser = user.copy(authentication = newAuth, admin = true, active = true)
    val userCountBeforeDelete = userDAO.findAll().length
    userDAO.delete(tobeDeletedUser.id)
    val deletedUser  = userDAO.findById(user.id)
    val userCountAfterDelete = userDAO.findAll().length    
    // then
    assert(deletedUser === None , "Deletion was attempted but found  " + deletedUser)
    userCountAfterDelete should be(userCountBeforeDelete -1)
    
  }
  
  "rememberNotifications" should "store dates properly" taggedAs RequiresDb in {
    // given
    val user = UserAssembler.randomUser.get
    userDAO.add(user)
    val followupDate = DateTime.now()
    val notifications = LastUserNotificationDispatch(None, Some(followupDate))

    // when
    userDAO.rememberNotifications(user.id, notifications)

    // then
    val foundUser = userDAO.findById(user.id)
    foundUser.get.notifications.commits should equal(None)
    foundUser.get.notifications.followups.get.getMillis should equal(followupDate.getMillis)
  }

  it should "update existing dates" taggedAs RequiresDb in {
    // given
    val notifications = LastUserNotificationDispatch(Some(DateTime.now().minusHours(5)), Some(DateTime.now().minusMinutes(5)))
    val user = UserAssembler.randomUser.withNotificationsDispatch(notifications).get
    userDAO.add(user)

    // when
    val commitDate = DateTime.now().minusMinutes(1)
    val followupDate = DateTime.now().minusMinutes(1)
    val newNotifications = LastUserNotificationDispatch(Some(commitDate), Some(followupDate))
    userDAO.rememberNotifications(user.id, newNotifications)

    // then
    val foundUser = userDAO.findById(user.id)
    foundUser.get.notifications.commits.get.getMillis should equal(commitDate.getMillis)
    foundUser.get.notifications.followups.get.getMillis should equal(followupDate.getMillis)
  }

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

  it should "allow to review start date be empty" taggedAs RequiresDb in {
    // given
    val userWithEmptyDate = user.copy(settings = user.settings.copy(toReviewStartDate = None))
    userDAO.add(userWithEmptyDate)

    // when
    val Some(found) = userDAO.findById(user.id)

    // then
    found.settings.toReviewStartDate should be('empty)
  }

  it should "set to review start date for user" taggedAs RequiresDb in {
    // given
    userDAO.add(user)

    // when
    val dateToSet = clock.nowUtc
    userDAO.setToReviewStartDate(user.id, dateToSet)

    // then
    val Some(found) = userDAO.findById(user.id)
    found.settings.toReviewStartDate should be(Some(dateToSet))
  }

  it should "find partial details by name/email" taggedAs RequiresDb in {
    // when
    val partial1 = userDAO.findPartialUserDetails(List("User Name 1"), List("user1@sml.com", "user2@sml.com"))
    val partial2 = userDAO.findPartialUserDetails(List("User Name 1", "User Name 3"), Nil)

    // then
    partial1.map(_.name).toSet should be (Set("User Name 1", "User Name 2"))
    partial2.map(_.name).toSet should be (Set("User Name 1", "User Name 3"))
  }

  it should "find partial details by id" taggedAs RequiresDb in {
    // given
    val users = userDAO.findAll().take(2)
    val ids = users.map(_.id)

    // when
    val partial = userDAO.findPartialUserDetails(ids)

    // then
    partial should have size (2)
    partial.map(_.name).toSet should be (users.map(_.name).toSet)
  }
  
}