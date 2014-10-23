package com.softwaremill.codebrag.migration

import com.softwaremill.codebrag.common.RealTimeClock
import com.softwaremill.codebrag.dao.branch.SQLWatchedBranchesDao
import com.softwaremill.codebrag.dao.repo.SQLUserRepoDetailsDAO
import com.softwaremill.codebrag.dao.sql.SQLDatabase
import com.softwaremill.codebrag.dao.{DaoConfig, Daos}
import com.softwaremill.codebrag.domain.UserWatchedBranch
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.slf4j.Logging
import org.bson.types.ObjectId

import scala.slick.jdbc.{StaticQuery => Q}

object MigrateV2_2ToV2_3 extends App with Logging {

  val baseConfig = ConfigFactory.load()
  val config = new DaoConfig {
    def rootConfig = baseConfig
  }

  val sqlDb = SQLDatabase.createEmbedded(config)
  val sqlDaos = new Daos {
    def clock = RealTimeClock
    def sqlDatabase = sqlDb
  }

  val userRepoDetailsDao = new SQLUserRepoDetailsDAO(sqlDb)
  var watchedBranchesDao = new SQLWatchedBranchesDao(sqlDb)

  sqlDb.db.withDynSession {
    logger.debug("Updating database schema")
    sqlDb.updateSchema()
    logger.debug(s"Updating watched branches")
    userRepoDetailsDao.findAll().map { userRepo =>
      val branchName = if (userRepo.branchName.isEmpty) "master" else userRepo.branchName
      UserWatchedBranch(new ObjectId(), userRepo.userId, userRepo.repoName, branchName)
    } foreach watchedBranchesDao.save
    logger.debug(s"Updating watched branches - Done")
  }
}
