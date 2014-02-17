package com.softwaremill.codebrag.dao.sql

import com.softwaremill.codebrag.dao.{MongoDaos, SQLDaos, DaoConfig}
import com.typesafe.config.ConfigFactory
import com.softwaremill.codebrag.common.{StatisticEvent, RealTimeClock}
import com.softwaremill.codebrag.dao.mongo.MongoInit
import com.foursquare.rogue.LiftRogue._
import com.softwaremill.codebrag.dao.user.{LikeRecord, CommentRecord, InternalUserRecord}
import com.softwaremill.codebrag.domain._
import com.softwaremill.codebrag.dao.events.EventRecord
import org.joda.time.DateTime
import com.softwaremill.codebrag.dao.followup.{SQLFollowupSchema, FollowupRecord}
import com.softwaremill.codebrag.dao.heartbeat.HeartbeatRecord
import com.softwaremill.codebrag.dao.invitation.InvitationRecord
import com.softwaremill.codebrag.dao.repositorystatus.RepositoryStatusRecord
import com.softwaremill.codebrag.domain.Comment
import com.softwaremill.codebrag.domain.Like
import com.softwaremill.codebrag.domain.Invitation
import com.softwaremill.codebrag.dao.reviewtask.CommitReviewTaskRecord

object MigrateMongoToSQL extends App {
  val config = new DaoConfig {
    def rootConfig = ConfigFactory.load()
  }

  SQLDatabase.updateSchema(config)
  val sqlDb = SQLDatabase.createEmbedded(config)
  val sqlDaos = new SQLDaos {
    def clock = RealTimeClock
    def sqlDatabase = sqlDb
  }

  MongoInit.initialize(config)
  val mongoDaos = new MongoDaos {
    def clock = RealTimeClock
  }

  def migrateUsers() {
    println("Migrating users ...")
    mongoDaos.userDao.findAll().foreach(sqlDaos.userDao.add)
    InternalUserRecord.where(_.internal eqs true)
      .fetch()
      .map(iu => InternalUser(iu.id.get, iu.name.get))
      .foreach(sqlDaos.internalUserDao.createIfNotExists)
  }

  def migrateCommits() {
    println("Migrating commits ...")
    for {
      id <- mongoDaos.commitInfoDao.findAllIds()
      commitInfo <- mongoDaos.commitInfoDao.findByCommitId(id)
    } {
      sqlDaos.commitInfoDao.storeCommit(commitInfo)
    }
  }

  def migrateEvents() {
    println("Migrating events ...")
    EventRecord.fetchBatch(1000) { batch =>
      batch.foreach { event =>
        sqlDaos.eventDao.storeEvent(new StatisticEvent {
          def toEventStream = ""
          def userId = event.originatingUserId.get
          def timestamp = new DateTime(event.date.get)
          def eventType = event.eventType.get
        })
      }
      Nil
    }
  }

  def migrateFollowups() {
    println("Migrating followups ...")

    class T(val database: SQLDatabase) extends SQLFollowupSchema {
      import database.driver.simple._
      import database._
      def saveFollowup(r: FollowupRecord) {
        db.withTransaction { implicit session =>
        followups += SQLFollowup(
          r.id.get,
          r.receivingUserId.get,
          r.threadId.get.commitId.get,
          r.threadId.get.fileName.get,
          r.threadId.get.lineNumber.get,
          r.lastReaction.get.reactionId.get,
          new DateTime(r.lastReaction.get.reactionId.get.getTime),
          r.lastReaction.get.reactionAuthorId.get
        )
      }
    }
    }
    val t = new T(sqlDb)

    FollowupRecord.fetchBatch(100) { batch =>
      batch.foreach { followup =>
        t.saveFollowup(followup)
      }
      Nil
    }
  }

  def migrateInvitations() {
    println("Migrating invitations ...")

    InvitationRecord.fetch().foreach { i =>
      sqlDaos.invitationDao.save(Invitation(i.code.get, i.invitationSender.get, new DateTime(i.expiryDate.get)))
    }
  }

  def migrateComments() {
    println("Migrating comments ... ")

    CommentRecord.fetchBatch(100) { batch =>
      batch.foreach { c =>
        sqlDaos.commentDao.save(Comment(c.id.get, c.commitId.get, c.authorId.get, new DateTime(c.date.get),
          c.message.get, c.fileName.get, c.lineNumber.get))
      }
      Nil
    }
  }

  def migrateLikes() {
    println("Migrating likes ... ")

    LikeRecord.fetchBatch(100) { batch =>
      batch.foreach { l =>
        sqlDaos.likeDao.save(Like(l.id.get, l.commitId.get, l.authorId.get, new DateTime(l.date.get),
          l.fileName.get, l.lineNumber.get))
      }
      Nil
    }
  }

  def migrateRepositoryStatus() {
    println("Migrating repository statuses ... ")

    RepositoryStatusRecord.fetch().foreach { rs =>
      sqlDaos.repoStatusDao.updateRepoStatus(RepositoryStatus(rs.repoName.get, rs.sha.get, rs.repoReady.get,
        rs.repoStatusError.get))
    }
  }

  def migrateReviewTasks() {
    println("Migrating review tasks ... ")

    CommitReviewTaskRecord.fetchBatch(100) { batch =>
      batch.foreach { rt =>
        sqlDaos.commitReviewTaskDao.save(CommitReviewTask(rt.commitId.get, rt.userId.get))
      }
      Nil
    }
  }

  // Not migrating:
  // - heartbeats
  // - instance settings

  migrateUsers()
  migrateEvents()
  migrateInvitations()

  migrateCommits()
  migrateComments()
  migrateLikes()
  migrateFollowups()
  migrateReviewTasks()

  migrateRepositoryStatus()
}
