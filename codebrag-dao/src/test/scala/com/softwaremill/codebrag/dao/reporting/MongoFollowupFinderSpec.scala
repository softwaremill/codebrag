package com.softwaremill.codebrag.dao.reporting

import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.Followup
import org.joda.time.DateTime
import com.softwaremill.codebrag.domain.builder.CommitInfoAssembler
import com.softwaremill.codebrag.test.mongo.ClearDataAfterTest
import com.softwaremill.codebrag.domain.ThreadDetails
import scala.Some
import com.softwaremill.codebrag.dao._
import com.foursquare.rogue.LiftRogue._
import org.bson.types.ObjectId

class MongoFollowupFinderSpec extends FlatSpecWithMongo with ClearDataAfterTest with ShouldMatchers with MongoFollowupFinderSpecFixture {

  var followupDao: FollowupDAO = _
  var commitInfoDao: CommitInfoDAO = _
  var followupFinder: FollowupFinder = _

  override def beforeEach() {
    super.beforeEach()
    followupDao = new MongoFollowupDAO
    followupFinder = new MongoFollowupFinder
    commitInfoDao = new MongoCommitInfoDAO
    storeAllCommits
  }

  it should "find all follow-ups only for given user" taggedAs(RequiresDb) in {
    // given
    storeFollowupsForJohn
    storeFollowupForBob

    // when
    val userFollowups = followupFinder.findAllFollowupsForUser(JohnId)

    // then
    userFollowups.followups should have size(2)
  }

  it should "find followup by id for given user" taggedAs(RequiresDb) in {
    // given
    val storedFollowupsId = storeFollowupsForJohn
    val followupIdToFind = storedFollowupsId.head

    // when & then
    val Right(followupFound) = followupFinder.findFollowupForUser(JohnId, followupIdToFind)
  }

  it should "not find followup by id for another user" taggedAs(RequiresDb) in {
    // given
    val followupsStored = storeFollowupsForJohn
    val followupIdToFind = followupsStored.head

    // when & then
    val Left(msg) = followupFinder.findFollowupForUser(BobId, followupIdToFind)
  }

  it should "return user follow-ups with newest first order" taggedAs(RequiresDb) in {
    // given
    storeFollowupsForJohn

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

  def storeFollowupsForJohn: List[ObjectId] = {
    val followups = List(
      Followup.forComment(FixtureCommit1.id, JohnId, date, LastCommenterName, ThreadDetails(FixtureCommit1.id)),
      Followup.forComment(FixtureCommit2.id, JohnId, laterDate, LastCommenterName, ThreadDetails(FixtureCommit2.id)))
    followups.foreach(followupDao.createOrUpdateExisting(_))
    FollowupRecord.where(_.user_id eqs JohnId).fetch().map(_.followupId.get)
  }

  def storeFollowupForBob: Followup = {
    val followup = Followup.forComment(FixtureCommit3.id, BobId, latestDate, LastCommenterName, ThreadDetails(FixtureCommit3.id))
    followupDao.createOrUpdateExisting(followup)
    followup
  }

  def storeAllCommits {
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
