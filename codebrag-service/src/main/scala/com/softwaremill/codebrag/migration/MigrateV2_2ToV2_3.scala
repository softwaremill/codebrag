package com.softwaremill.codebrag.migration

import com.softwaremill.codebrag.domain.UserWatchedBranch
import org.bson.types.ObjectId

object MigrateV2_2ToV2_3 extends App with MigrationConfig {

  import daos._

  sqlDatabase.db.withDynSession {
    logger.debug("Updating database schema")
    sqlDatabase.updateSchema()
    logger.debug(s"Updating watched branches")
    userRepoDetailsDao.findAll().map { userRepo =>
      val branchName = if (userRepo.branchName.isEmpty) "master" else userRepo.branchName
      UserWatchedBranch(new ObjectId(), userRepo.userId, userRepo.repoName, branchName)
    } foreach userObservedBranchesDao.save
    logger.debug(s"Updating watched branches - Done")
  }
}
