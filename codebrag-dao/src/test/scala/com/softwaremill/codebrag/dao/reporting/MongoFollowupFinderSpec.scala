package com.softwaremill.codebrag.dao.reporting

import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain._
import org.joda.time.DateTime
import com.softwaremill.codebrag.domain.builder.{UserAssembler, CommitInfoAssembler}
import com.softwaremill.codebrag.test.mongo.ClearDataAfterTest
import scala.Some
import com.softwaremill.codebrag.dao._
import org.bson.types.ObjectId
import com.softwaremill.codebrag.builders.CommentAssembler

class MongoFollowupFinderSpec extends FlatSpecWithMongo with ClearDataAfterTest with ShouldMatchers {

  var followupDao: FollowupDAO = _
  var commitInfoDao: CommitInfoDAO = _
  var commentDao: CommitCommentDAO = _
  var followupFinder: FollowupFinder = _
  var userDao: UserDAO = _

  val JohnId = ObjectIdTestUtils.oid(12)
  val BobId = ObjectIdTestUtils.oid(25)

  override def beforeEach() {
    super.beforeEach()
    followupDao = new MongoFollowupDAO
    followupFinder = new MongoFollowupFinder
    commitInfoDao = new MongoCommitInfoDAO
    userDao = new MongoUserDAO
    commentDao = new MongoCommitCommentDAO
  }

  case class CreatedFollowup(id: ObjectId, followup: Followup, reaction: Comment, reactionAuthor: User, commit: CommitInfo)

  def createFollowupWithDependenciesFor(receivingUserId: ObjectId, reactionDate: DateTime = DateTime.now) = {
    val commit = CommitInfoAssembler.randomCommit.get
    commitInfoDao.storeCommit(commit)

    val user = UserAssembler.randomUser.get
    userDao.add(user)

    val comment = CommentAssembler.commentFor(commit.id).withAuthorId(user.id).withDate(reactionDate).get
    commentDao.save(comment)

    val followupToCreate = Followup(receivingUserId, comment)
    val followupId = followupDao.createOrUpdateExisting(followupToCreate)
    CreatedFollowup(followupId, followupToCreate, comment, user, commit)
  }

  it should "find all follow-ups only for given user" taggedAs(RequiresDb) in {
    // given
    val johnFollowup = createFollowupWithDependenciesFor(JohnId)
    createFollowupWithDependenciesFor(BobId)

    // when
    val userFollowups = followupFinder.findAllFollowupsForUser(JohnId)

    // then
    userFollowups.followups should have size 1
    userFollowups.followups.head.followupId should be(johnFollowup.id.toString)
  }

  it should "find followup by id for given user" taggedAs(RequiresDb) in {
    // given
    val johnFollowup = createFollowupWithDependenciesFor(JohnId)

    // when
    val Right(found) = followupFinder.findFollowupForUser(JohnId, johnFollowup.id)

    // then
    found.followupId should be(johnFollowup.id.toString)
  }

  it should "not find followup by id for another user" taggedAs(RequiresDb) in {
    // given
    val johnFollowup = createFollowupWithDependenciesFor(JohnId)

    // when & then
    val Left(msg) = followupFinder.findFollowupForUser(BobId, johnFollowup.id)
  }

  it should "return user follow-ups with newest first order" taggedAs(RequiresDb) in {
    // given
    val date = DateTime.now
    createFollowupWithDependenciesFor(JohnId, date)
    createFollowupWithDependenciesFor(JohnId, date.plusHours(1))

    // when
    val userFollowups = followupFinder.findAllFollowupsForUser(JohnId).followups

    // then
    userFollowups(0).date should be > userFollowups(1).date
  }

  it should "return followup with commit data included" taggedAs(RequiresDb) in {
    // given
    val stored = createFollowupWithDependenciesFor(JohnId)

    // when
    val found = followupFinder.findAllFollowupsForUser(JohnId).followups.head

    // then
    found.commit.authorName should equal(stored.commit.authorName)
    found.commit.commitId should equal(stored.commit.id.toString)
    found.commit.date should equal(stored.commit.authorDate.toDate)
    found.commit.message should equal(stored.commit.message)
  }

  it should "return followup with last reaction and author data included" taggedAs(RequiresDb) in {
    // given
    val stored = createFollowupWithDependenciesFor(JohnId)

    // when
    val found = followupFinder.findAllFollowupsForUser(JohnId).followups.head

    // then
    found.reaction.reactionId should equal(stored.reaction.id.toString)
    found.reaction.reactionAuthor should equal(stored.reactionAuthor.name)
    found.reaction.reactionAuthorAvatarUrl should equal(Some(stored.reactionAuthor.avatarUrl))
  }
}
