package com.softwaremill.codebrag.dao.reporting

import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain._
import org.joda.time.DateTime
import com.softwaremill.codebrag.domain.builder.{CommentAssembler, UserAssembler, CommitInfoAssembler}
import com.softwaremill.codebrag.test.mongo.ClearDataAfterTest
import scala.Some
import com.softwaremill.codebrag.dao._
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.reporting.views.{FollowupLastLikeView, FollowupLastCommentView}

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

  def createFollowupWithDependenciesFor(commit: CommitInfo, receivingUserId: ObjectId, reactionDate: DateTime = DateTime.now, threadDetails: ThreadDetails = ThreadDetails(new ObjectId, None, None)) = {
    commitInfoDao.storeCommit(commit)

    val user = UserAssembler.randomUser.get
    userDao.add(user)

    val base = CommentAssembler.commentFor(commit.id).withAuthorId(user.id).withDate(reactionDate)
    val comment = (threadDetails.fileName, threadDetails.lineNumber) match {
      case (Some(file), Some(line)) => base.withFileNameAndLineNumber(file, line).get
      case _ => base.get
    }

    commentDao.save(comment)

    val followupToCreate = Followup(receivingUserId, comment)
    val followupId = followupDao.createOrUpdateExisting(followupToCreate)
    CreatedFollowup(followupId, followupToCreate, comment, user, commit)
  }

  it should "have followups for given commit grouped together" in {
    // given
    val now = DateTime.now
    val commit = CommitInfoAssembler.randomCommit.get
    val entireCommitFollowup = createFollowupWithDependenciesFor(commit, JohnId, now, ThreadDetails(commit.id))
    val firstInlineFollowup = createFollowupWithDependenciesFor(commit, JohnId, now.plusHours(2), ThreadDetails(commit.id, Some(20), Some("file.txt")))

    val anotherCommit = CommitInfoAssembler.randomCommit.get
    val anotherCommitFollowup = createFollowupWithDependenciesFor(anotherCommit, JohnId, now.plusHours(1), ThreadDetails(anotherCommit.id))


    // when
    val result = followupFinder.findAllFollowupsByCommitForUser(JohnId)

    // then
    result.followupsByCommit.size should be(2)

    val followupsForFirstCommit = result.followupsByCommit(0).followups
    followupsForFirstCommit.size should be(2)
    followupsForFirstCommit.map(_.followupId).toSet should be(Set(entireCommitFollowup.id.toString, firstInlineFollowup.id.toString))

    val followupsForSecondCommit = result.followupsByCommit(1).followups
    followupsForSecondCommit.size should be(1)
    followupsForSecondCommit.map(_.followupId).toSet should be(Set(anotherCommitFollowup.id.toString))
  }

  it should "have correct commit data for followup groups" in {
    // given
    val now = DateTime.now
    val commit = CommitInfoAssembler.randomCommit.get
    createFollowupWithDependenciesFor(commit, JohnId, now, ThreadDetails(commit.id))

    val anotherCommit = CommitInfoAssembler.randomCommit.get
    createFollowupWithDependenciesFor(anotherCommit, JohnId, now.plusHours(1), ThreadDetails(anotherCommit.id))

    // when
    val result = followupFinder.findAllFollowupsByCommitForUser(JohnId)

    // then
    result.followupsByCommit.size should be(2)

    val forAnotherCommit = result.followupsByCommit(0)
    forAnotherCommit.commit.commitId should be(anotherCommit.id.toString)
    forAnotherCommit.commit.authorName should be(anotherCommit.authorName)
    forAnotherCommit.commit.date should be(anotherCommit.authorDate.toDate)
    forAnotherCommit.commit.message should be(anotherCommit.message)

    val forFirstCommit = result.followupsByCommit(1)
    forFirstCommit.commit.commitId should be(commit.id.toString)
    forFirstCommit.commit.authorName should be(commit.authorName)
    forFirstCommit.commit.date should be(commit.authorDate.toDate)
    forFirstCommit.commit.message should be(commit.message)
  }

  it should "have correct reactions data for followup" in {
    // given
    val now = DateTime.now
    val commit = CommitInfoAssembler.randomCommit.get
    val firstInlineFollowup = createFollowupWithDependenciesFor(commit, JohnId, now.plusHours(1), ThreadDetails(commit.id, Some(20), Some("file.txt")))
    val secondInlineFollowup = createFollowupWithDependenciesFor(commit, JohnId, now.plusHours(2), ThreadDetails(commit.id, Some(20), Some("file.txt")))

    // when
    val result = followupFinder.findAllFollowupsByCommitForUser(JohnId)

    // then
    val inlineFollowup = result.followupsByCommit.head.followups.head

    inlineFollowup.allReactions should be(List(firstInlineFollowup.reaction.id.toString, secondInlineFollowup.reaction.id.toString))
    inlineFollowup.followupId should be(secondInlineFollowup.id.toString)

    inlineFollowup.lastReaction.date should be(secondInlineFollowup.reaction.postingTime.toDate)
    inlineFollowup.lastReaction.reactionAuthor should be(secondInlineFollowup.reactionAuthor.name)
    inlineFollowup.lastReaction.reactionAuthorAvatarUrl should be(secondInlineFollowup.reactionAuthor.avatarUrl)
    inlineFollowup.lastReaction.reactionId should be(secondInlineFollowup.reaction.id.toString)
  }

  it should "find all follow-ups only for given user" taggedAs(RequiresDb) in {
    // given
    val commit = CommitInfoAssembler.randomCommit.get
    val johnFollowup = createFollowupWithDependenciesFor(commit, JohnId)
    createFollowupWithDependenciesFor(commit, BobId)

    // when
    val userFollowups = followupFinder.findAllFollowupsByCommitForUser(JohnId)

    // then
    userFollowups.followupsByCommit should have size 1
    val firstFollowupOnList = userFollowups.followupsByCommit.head.followups.head
    firstFollowupOnList.followupId should be(johnFollowup.id.toString)
  }

  it should "find followup by id for given user" taggedAs(RequiresDb) in {
    // given
    val commit = CommitInfoAssembler.randomCommit.get
    val johnFollowup = createFollowupWithDependenciesFor(commit, JohnId)

    // when
    val Right(found) = followupFinder.findFollowupForUser(JohnId, johnFollowup.id)

    // then
    found.followupId should be(johnFollowup.id.toString)
  }

  it should "not find followup by id for another user" taggedAs(RequiresDb) in {
    // given
    val commit = CommitInfoAssembler.randomCommit.get
    val johnFollowup = createFollowupWithDependenciesFor(commit, JohnId)

    // when & then
    val Left(msg) = followupFinder.findFollowupForUser(BobId, johnFollowup.id)
  }

  it should "have follow-ups for commit in newest-first order" taggedAs(RequiresDb) in {
    // given
    val now = DateTime.now
    val commit = CommitInfoAssembler.randomCommit.get
    createFollowupWithDependenciesFor(commit, JohnId, now, ThreadDetails(commit.id))
    createFollowupWithDependenciesFor(commit, JohnId, now.plusHours(2), ThreadDetails(commit.id, Some(20), Some("file.txt")))


    // when
    val result = followupFinder.findAllFollowupsByCommitForUser(JohnId)

    // then
    val followupsForCommit = result.followupsByCommit(0).followups
    followupsForCommit(0).lastReaction.date should be > followupsForCommit(1).lastReaction.date
  }

  it should "have commits in newest-followup-first order" taggedAs(RequiresDb) in {
    // given
    val now = DateTime.now

    val commit = CommitInfoAssembler.randomCommit.get
    createFollowupWithDependenciesFor(commit, JohnId, now, ThreadDetails(commit.id))

    val anotherCommit = CommitInfoAssembler.randomCommit.get
    createFollowupWithDependenciesFor(anotherCommit, JohnId, now.plusHours(1), ThreadDetails(anotherCommit.id))

    // when
    val result = followupFinder.findAllFollowupsByCommitForUser(JohnId)

    // then
    result.followupsByCommit(0).commit.commitId should be(anotherCommit.id.toString)
    result.followupsByCommit(1).commit.commitId should be(commit.id.toString)
  }

  it should "have reaction containing valid type and comment message" in {
    // given
    val now = DateTime.now
    val commit = CommitInfoAssembler.randomCommit.get
    val created = createFollowupWithDependenciesFor(commit, JohnId, now, ThreadDetails(commit.id))

    // when
    val result = followupFinder.findAllFollowupsByCommitForUser(JohnId)

    // then
    val reaction = result.followupsByCommit(0).followups(0).lastReaction.asInstanceOf[FollowupLastCommentView]
    reaction.message should be(created.reaction.message)
  }

}