package com.softwaremill.codebrag.dao.repositorystatus

import com.softwaremill.codebrag.domain.RepositoryStatus

trait RepositoryStatusDAO {
   def update(repoName: String, newSha: String)
   def get(repoName: String): Option[String]

   def updateRepoStatus(newStatus: RepositoryStatus)
   def getRepoStatus(repoName: String): Option[RepositoryStatus]
 }
