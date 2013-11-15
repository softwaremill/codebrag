package com.softwaremill.codebrag.service.followups

import com.softwaremill.codebrag.dao._
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain._
import org.joda.time.DateTime
import com.softwaremill.codebrag.domain.Comment
import com.softwaremill.codebrag.domain.Followup
import com.softwaremill.codebrag.dao.events.NewUserRegistered

class WelcomeFollowupsGenerator(internalUserDao: MongoInternalUserDAO,
                                 commentsDao: CommitCommentDAO,
                                 likesDao: LikeDAO,
                                 followupsDao: FollowupDAO,
                                 commitInfoDao: CommitInfoDAO) {

  def createWelcomeFollowupFor(newUser: NewUserRegistered) {
    internalUserDao.findByName(InternalUser.WelcomeFollowupsAuthorName).foreach { codebragInternalUser =>
      val userLastCommits = commitInfoDao.findLastCommitsAuthoredByUser(newUser, 2)
      if (userLastCommits.size > 0) {
        val commitForLike = userLastCommits.head
        val commitForComment = userLastCommits.tail match {
          case Nil => commitForLike
          case rest => rest.head
        }
        createWelcomeLike(commitForLike, newUser, codebragInternalUser)
        createWelcomeComment(commitForComment, newUser, codebragInternalUser)
      }
    }
  }

  private def createWelcomeComment(commit: CommitInfo, registeredUser: NewUserRegistered, codebragUser: InternalUser) {
    val comment = Comment(new ObjectId, commit.id, codebragUser.id, DateTime.now, "Hey there. This is **codebrag**")
    commentsDao.save(comment)
    followupsDao.createOrUpdateExisting(Followup(registeredUser.id, comment))
  }

  private def createWelcomeLike(commit: CommitInfo, registeredUser: NewUserRegistered, codebragUser: InternalUser) {
    val like = Like(new ObjectId, commit.id, codebragUser.id, DateTime.now)
    likesDao.save(like)
    followupsDao.createOrUpdateExisting(Followup(registeredUser.id, like))
  }

}
