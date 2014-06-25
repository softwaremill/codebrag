package com.softwaremill.codebrag.migration

import com.softwaremill.codebrag.dao.{Daos, DaoConfig}
import com.typesafe.config.ConfigFactory
import com.softwaremill.codebrag.common.RealTimeClock
import com.softwaremill.codebrag.dao.branchsnapshot.SQLBranchStateDAO
import com.softwaremill.codebrag.dao.sql.SQLDatabase
import com.softwaremill.codebrag.repository.config.RepoDataDiscovery
import com.softwaremill.codebrag.service.config.RepositoryConfig
import scala.slick.jdbc.{StaticQuery => Q}
import scala.slick.driver.JdbcDriver.backend.Database
import Database.dynamicSession
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.user.SQLUserDAO
import com.softwaremill.codebrag.domain.UserRepoDetails
import com.softwaremill.codebrag.dao.repo.SQLUserRepoDetailsDAO

object MigrateV2_1ToV2_2 extends App with Logging {

  val baseConfig = ConfigFactory.load()
  val config = new DaoConfig {
    def rootConfig = baseConfig
  }

  val sqlDb = SQLDatabase.createEmbedded(config)
  val sqlDaos = new Daos {
    def clock = RealTimeClock
    def sqlDatabase = sqlDb
  }

  val userDao = new SQLUserDAO(sqlDb)
  val userRepoDetailsDao = new SQLUserRepoDetailsDAO(sqlDb)

  val repositories = RepoDataDiscovery.discoverRepoDataFromConfig(new RepositoryConfig {
    override def rootConfig = baseConfig
  })

  if(repositories.size != 1) {
    throw new IllegalStateException("There is more than one repository checked out. Cannot continue.")
  }

  val repositoryName = repositories.head.repoName
  sqlDb.db.withDynSession {

    logger.debug(s"Setting repo to current branches states")
    (Q.u + s"""ALTER TABLE "branch_states" DROP CONSTRAINT IF EXISTS "branch_states_id"""").execute()
    (Q.u + s"""ALTER TABLE "branch_states" ADD COLUMN IF NOT EXISTS "repo_name" VARCHAR DEFAULT '$repositoryName' NOT NULL""").execute()
    (Q.u + s"""ALTER TABLE "branch_states" ADD CONSTRAINT IF NOT EXISTS "repo_branch_state" UNIQUE("repo_name", "branch_name")""").execute()
    logger.debug(s"Setting repo for current branches states - Done")

    logger.debug(s"Adding repo name for already stored commits")
    (Q.u + s"""ALTER TABLE "commit_infos" DROP CONSTRAINT IF EXISTS "unique_sha"""").execute()
    (Q.u + s"""ALTER TABLE "commit_infos" ADD COLUMN IF NOT EXISTS "repo_name" VARCHAR DEFAULT '$repositoryName' NOT NULL""").execute()
    (Q.u + s"""ALTER TABLE "commit_infos" ADD CONSTRAINT IF NOT EXISTS "repo_sha" UNIQUE("repo_name", "sha")""").execute()
    logger.debug(s"Adding repo name to already stored commits - Done")

    logger.debug(s"Moving user/repo data")
    (Q.u + s"""CREATE TABLE "user_repo_details"("user_id" VARCHAR NOT NULL, "repo_name" VARCHAR NOT NULL, "branch_name" VARCHAR NOT NULL, "to_review_since" TIMESTAMP NOT NULL, "default" BOOLEAN NOT NULL DEFAULT FALSE)""").execute()
    (Q.u + s"""ALTER TABLE "user_repo_details" ADD CONSTRAINT "user_repo_branch_pk" PRIMARY KEY("user_id", "repo_name", "branch_name")""").execute()
    userDao.findAll().map { user =>
      UserRepoDetails(user.id, repositoryName, "master", user.settings.toReviewStartDate.getOrElse(RealTimeClock.nowUtc), true)
    } foreach userRepoDetailsDao.save
    logger.debug(s"Moving user/repo data - Done")


    logger.debug(s"Assigning already reviewed commits to current repo")
    (Q.u + s"""ALTER TABLE "reviewed_commits" DROP CONSTRAINT IF EXISTS "reviewed_commits_id"""").execute()
    (Q.u + s"""ALTER TABLE "reviewed_commits" ADD COLUMN IF NOT EXISTS "repo_name" VARCHAR NOT NULL DEFAULT '$repositoryName'""").execute()
    (Q.u + s"""ALTER TABLE "reviewed_commits" ADD CONSTRAINT IF NOT EXISTS "reviewed_commits_id" PRIMARY KEY ("user_id", "sha", "repo_name")""").execute()
    logger.debug(s"Assigning already reviewed commits to current repo - Done")
  }
}
