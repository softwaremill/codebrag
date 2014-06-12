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

  val branchStateDao = new SQLBranchStateDAO(sqlDb)
  val repositories = RepoDataDiscovery.discoverRepoDataFromConfig(new RepositoryConfig {
    override def rootConfig = baseConfig
  })

  if(repositories.size != 1) {
    throw new IllegalStateException("There is more than one repository checked out. Cannot continue.")
  }

  val repositoryName = repositories.head.repoName
  sqlDb.db.withDynSession {

    logger.debug(s"Setting repo to current branches states")
    (Q.u + s"""ALTER TABLE "branch_states" ADD COLUMN IF NOT EXISTS "repo_name" VARCHAR DEFAULT '$repositoryName' NOT NULL""").execute()
    (Q.u + s"""ALTER TABLE "branch_states" ADD CONSTRAINT IF NOT EXISTS "repo_branch_state" UNIQUE("repo_name", "branch_name")""").execute()
    logger.debug(s"Setting repo for current branches states - Done")

    logger.debug(s"Adding repo name for already stored commits")
    (Q.u + s"""ALTER TABLE "commit_infos" ADD COLUMN IF NOT EXISTS "repo_name" VARCHAR DEFAULT '$repositoryName' NOT NULL""").execute()
    (Q.u + s"""ALTER TABLE "commit_infos" ADD CONSTRAINT IF NOT EXISTS "repo_sha" UNIQUE("repo_name", "sha")""").execute()
    logger.debug(s"Adding repo name to already stored commits - Done")
  }
}
