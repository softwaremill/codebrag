package com.softwaremill.codebrag.service.followups

import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import com.softwaremill.codebrag.common.ClockSpec
import com.softwaremill.codebrag.dao._
import com.softwaremill.codebrag.service.config.{ConfigWithDefault, CodebragConfig}
import com.typesafe.config.ConfigFactory
import com.softwaremill.codebrag.dao.events.NewUserRegistered
import com.softwaremill.codebrag.domain.builder.{LikeAssembler, CommentAssembler, CommitInfoAssembler, UserAssembler}
import org.mockito.Mockito._
import org.mockito.ArgumentCaptor
import com.softwaremill.codebrag.domain.Followup
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.dao.reaction.{MongoLikeDAO, LikeDAO, CommitCommentDAO}
import com.softwaremill.codebrag.dao.followup.FollowupDAO

class FollowupsGeneratorForReactionsPriorUserRegistrationSpec extends FlatSpec with ShouldMatchers with BeforeAndAfterEach with MockitoSugar with ClockSpec {

  var commentsDao: CommitCommentDAO = _
  var likesDao: LikeDAO = _
  var followupsDao: FollowupDAO = _
  var commitInfoDao: CommitInfoDAO = _

  val config = new CodebragConfig with ConfigWithDefault {
    import collection.JavaConversions._
    def rootConfig = ConfigFactory.parseMap(Map.empty[String, String])
  }

  var followupsGenerator: FollowupsGeneratorForReactionsPriorUserRegistration = _

  val UserBob = NewUserRegistered(UserAssembler.randomUser.get)
  val ConfiguredTimeBack = clock.now.minusDays(config.replayFollowupsForPastCommitsTimeInDays)
  val BobCommits = List(CommitInfoAssembler.randomCommit.withAuthorEmail(UserBob.email).withAuthorName(UserBob.fullName).get)
  val BobCommitsIds = BobCommits.map(_.id)

  override def beforeEach() {
    commentsDao = mock[CommitCommentDAO]
    likesDao = mock[MongoLikeDAO]
    followupsDao = mock[FollowupDAO]
    commitInfoDao = mock[CommitInfoDAO]

    followupsGenerator = new FollowupsGeneratorForReactionsPriorUserRegistration(
      commentsDao,
      likesDao,
      followupsDao,
      commitInfoDao,
      config
    )
  }

  it should "not recreate any followups if user has no commits in configured time" in {
    // given
    val user = NewUserRegistered(UserAssembler.randomUser.get)
    val timeBack = clock.now.minusDays(config.replayFollowupsForPastCommitsTimeInDays)
    when(commitInfoDao.findLastCommitsAuthoredByUserSince(user, timeBack)).thenReturn(List.empty)

    // when
    followupsGenerator.recreateFollowupsForPastComments(user)

    // then
    verifyZeroInteractions(followupsDao)
  }

  it should "not recreate any followups if user has no reactions to his commits" in {
    // given
    when(commitInfoDao.findLastCommitsAuthoredByUserSince(UserBob, ConfiguredTimeBack)).thenReturn(BobCommits)
    when(commentsDao.findCommentsForCommits(BobCommitsIds:_*)).thenReturn(List.empty)
    when(likesDao.findLikesForCommits(BobCommitsIds:_*)).thenReturn(List.empty)

    // when
    followupsGenerator.recreateFollowupsForPastComments(UserBob)

    // then
    verifyZeroInteractions(followupsDao)
  }

  it should "recreate followups for all reactions in order they were placed to user commits" in {
    // given
    when(commitInfoDao.findLastCommitsAuthoredByUserSince(UserBob, ConfiguredTimeBack)).thenReturn(BobCommits)
    val commentsToUserCommits = List(comment2minsAgo)
    val likesToUserCommits = List(like4minsAgo)
    when(commentsDao.findCommentsForCommits(BobCommitsIds:_*)).thenReturn(commentsToUserCommits)
    when(likesDao.findLikesForCommits(BobCommitsIds:_*)).thenReturn(likesToUserCommits)

    // when
    followupsGenerator.recreateFollowupsForPastComments(UserBob)

    // then
    val captor = ArgumentCaptor.forClass(classOf[Followup])
    verify(followupsDao, times(2)).createOrUpdateExisting(captor.capture())
    val expectedFirstFollowup = Followup(UserBob.id, likesToUserCommits.head)
    val expectedNextFollowup = Followup(UserBob.id, commentsToUserCommits.head)
    import scala.collection.JavaConversions._
    captor.getAllValues.toList should be(List(expectedFirstFollowup, expectedNextFollowup))
  }


  private def like4minsAgo = LikeAssembler.likeFor(BobCommitsIds.head).postedAt(clock.now.minusMinutes(4)).get
  private def comment2minsAgo = CommentAssembler.commentFor(BobCommitsIds.head).postedAt(clock.now.minusMinutes(2)).get
}
