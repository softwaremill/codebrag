package com.softwaremill.codebrag.dao.reporting

import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.{User, Followup, ThreadDetails}
import org.joda.time.DateTime
import com.softwaremill.codebrag.domain.builder.{UserAssembler, CommitInfoAssembler}
import com.softwaremill.codebrag.test.mongo.ClearDataAfterTest
import scala.Some
import com.softwaremill.codebrag.dao._
import com.foursquare.rogue.LiftRogue._
import org.bson.types.ObjectId

class MongoFollowupFinderSpec extends FlatSpecWithMongo with ClearDataAfterTest with ShouldMatchers with MongoFollowupFinderSpecFixture {

  var followupDao: FollowupDAO = _
  var commitInfoDao: CommitInfoDAO = _
  var followupFinder: FollowupFinder = _
  var userDao: UserDAO = _

  override def beforeEach() {
    super.beforeEach()
    followupDao = new MongoFollowupDAO
    followupFinder = new MongoFollowupFinder
    commitInfoDao = new MongoCommitInfoDAO
    userDao = new MongoUserDAO
    storeAllCommits
  }

  it should "find all follow-ups only for given user" taggedAs(RequiresDb) in {
    // given
    storeFollowupsForJohn()
    storeFollowupForBob

    // when
    val userFollowups = followupFinder.findAllFollowupsForUser(JohnId)

    // then
    userFollowups.followups should have size(2)
  }

  it should "find followup by id for given user" taggedAs(RequiresDb) in {
    // given
    val storedFollowupsId = storeFollowupsForJohn()
    val followupIdToFind = storedFollowupsId.head

    // when & then
    val Right(followupFound) = followupFinder.findFollowupForUser(JohnId, followupIdToFind)
  }

  it should "not find followup by id for another user" taggedAs(RequiresDb) in {
    // given
    val followupsStored = storeFollowupsForJohn()
    val followupIdToFind = followupsStored.head

    // when & then
    val Left(msg) = followupFinder.findFollowupForUser(BobId, followupIdToFind)
  }

  it should "Not load author avatar in a single follow-up if author no longer exists in DB" taggedAs(RequiresDb) in {
    // given
    val followupsStored = storeFollowupsForJohn()
    val followupIdToFind = followupsStored.head

    // when
    val Right(view) = followupFinder.findFollowupForUser(JohnId, followupIdToFind)

    // then
    view.reaction.reactionAuthorAvatarUrl should be (None)
  }

  it should "Not load author avatar in a follow-up list if author no longer exists in DB" taggedAs(RequiresDb) in {
    // given
    val storedFollowups = storeFollowupsForJohn()

    // when
    val view = followupFinder.findAllFollowupsForUser(JohnId)

    // then
    view.followups.size should equal(storedFollowups.size)
    view.followups foreach( followup => {
      followup.reaction.reactionAuthorAvatarUrl should be (None)
    })
  }

  it should "Load empty avatar url in a follow-up list if author exists in DB" taggedAs(RequiresDb) in {
    // given
    val authorId = new ObjectId
    val storedFollowups = storeFollowupsForJohn(authorId)
    val authorUser = UserAssembler.randomUser.withId(authorId).withAvatarUrl("").get
    userDao.add(authorUser)

    // when
    val view = followupFinder.findAllFollowupsForUser(JohnId)

    // then
    view.followups.size should equal(storedFollowups.size)
    view.followups foreach( followup => {
      followup.reaction.reactionAuthorAvatarUrl should be (None)
    })
  }

  it should "Load non-empty avatar urls in a follow-up list if author exists in DB" taggedAs(RequiresDb) in {
    // given
    val authorId = new ObjectId
    val authorId2 = new ObjectId

    val avatarUrl = "http://avatar.com/face.jpg"
    val avatarUrl2 = "http://avatar.com/face2.jpg"
    followupDao.createOrUpdateExisting(Followup.forComment(FixtureCommit1.id, authorId, JohnId, new DateTime, "author1", ThreadDetails(FixtureCommit1.id)))
    followupDao.createOrUpdateExisting(Followup.forComment(FixtureCommit2.id, authorId2, JohnId, new DateTime, "author2", ThreadDetails(FixtureCommit2.id)))
    followupDao.createOrUpdateExisting(Followup.forComment(FixtureCommit3.id, new ObjectId, JohnId, new DateTime, "last commenter", ThreadDetails(FixtureCommit3.id)))

    val authorUser1 = UserAssembler.randomUser.withAvatarUrl(avatarUrl).withId(authorId).get
    val authorUser2 = UserAssembler.randomUser.withAvatarUrl(avatarUrl2).withId(authorId2).get


    userDao.add(authorUser1)
    userDao.add(authorUser2)

    // when
    val view = followupFinder.findAllFollowupsForUser(JohnId)

    // then
    view.followups.size should equal(3)

    view.followups(0).reaction.reactionAuthorAvatarUrl should equal (None)
    view.followups(1).reaction.reactionAuthorAvatarUrl should equal (Some(avatarUrl2))
    view.followups(2).reaction.reactionAuthorAvatarUrl should equal (Some(avatarUrl))
  }

  it should "Load non-empty avatar url in a single follow-up if author exists in DB" taggedAs(RequiresDb) in {
    // given
    val authorId = new ObjectId
    val storedFollowups = storeFollowupsForJohn(authorId)
    val avatarUrl = "http://avatar.com/face.jpg"
    val authorUser = UserAssembler.randomUser.withId(authorId).withAvatarUrl(avatarUrl).get
    userDao.add(authorUser)

    // when
    val Right(view) = followupFinder.findFollowupForUser(JohnId, storedFollowups.head)

    // then
    view.reaction.reactionAuthorAvatarUrl should be (Some(avatarUrl))
  }

  it should "Load empty avatar url in a single follow-up if author exists in DB" taggedAs(RequiresDb) in {
    // given
    val authorId = new ObjectId
    val storedFollowups = storeFollowupsForJohn(authorId)
    val authorUser = UserAssembler.randomUser.withId(authorId).withAvatarUrl("").get
    userDao.add(authorUser)

    // when
    val Right(view) = followupFinder.findFollowupForUser(JohnId, storedFollowups.head)

    // then
    view.reaction.reactionAuthorAvatarUrl should be (None)
  }

  it should "return user follow-ups with newest first order" taggedAs(RequiresDb) in {
    // given
    storeFollowupsForJohn()

    // when
    val userFollowups = followupFinder.findAllFollowupsForUser(JohnId).followups

    // then
    userFollowups(First).date should equal(laterDate.toDate)
    userFollowups(Second).date should equal(date.toDate)
  }

  it should "return followup with last author name and comment id included" taggedAs(RequiresDb) in {
    // given
    val storedFollowupId = storeFollowupForBob

    // when
    val followup = followupFinder.findAllFollowupsForUser(BobId).followups.head

    // then
    followup.reaction.reactionAuthor should be(LastCommenterName)
    followup.reaction.reactionId should be(storedFollowupId.reactionId.toString)
  }

  def storeFollowupsForJohn(authorId: ObjectId = new ObjectId): List[ObjectId] = {
    val followups = List(
      Followup.forComment(FixtureCommit1.id, authorId, JohnId, date, LastCommenterName, ThreadDetails(FixtureCommit1.id)),
      Followup.forComment(FixtureCommit2.id, authorId, JohnId, laterDate, LastCommenterName, ThreadDetails(FixtureCommit2.id)))
    followups.foreach(followupDao.createOrUpdateExisting(_))
    FollowupRecord.where(_.user_id eqs JohnId).fetch().map(_.followupId.get)
  }

  def storeFollowupForBob: Followup = {
    val authorId = new ObjectId
    val followup = Followup.forComment(FixtureCommit3.id, authorId, BobId, latestDate, LastCommenterName, ThreadDetails(FixtureCommit3.id))
    followupDao.createOrUpdateExisting(followup)
    followup
  }

  def storeAllCommits() {
    List(FixtureCommit1, FixtureCommit2, FixtureCommit3).foreach(commitInfoDao.storeCommit(_))
  }

}

trait MongoFollowupFinderSpecFixture {

  val JohnId = ObjectIdTestUtils.oid(12)
  val BobId = ObjectIdTestUtils.oid(25)

  val date = DateTime.now()
  val laterDate = date.plusMinutes(1)
  val latestDate = date.plusMinutes(10)

  val LastCommenterName = "Mary"

  val First = 0
  val Second = 1

  val FixtureCommit1 = CommitInfoAssembler.randomCommit.get
  val FixtureCommit2 = CommitInfoAssembler.randomCommit.get
  val FixtureCommit3 = CommitInfoAssembler.randomCommit.get
}
