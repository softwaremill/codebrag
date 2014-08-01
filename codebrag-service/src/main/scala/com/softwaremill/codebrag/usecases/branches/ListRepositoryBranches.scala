package com.softwaremill.codebrag.usecases.branches

import com.softwaremill.codebrag.cache.RepositoriesCache
import com.softwaremill.codebrag.dao.branch.WatchedBranchesDao
import org.bson.types.ObjectId

case class SingleBranchView(branchName: String, watching: Boolean)
case class RepositoryBranchesView(branches: Seq[SingleBranchView], repoType: String)

class ListRepositoryBranches(repositoriesCache: RepositoriesCache, observedBranchesDao: WatchedBranchesDao) {

  def execute(userId: ObjectId, repoName: String) = {
    val repo = repositoriesCache.getRepo(repoName)
    val observed = observedBranchesDao.findAll(userId).filter(_.repoName == repoName)
    val branches = repo.getShortBranchNames.toSeq.sorted.map(b => SingleBranchView(b, observed.exists(_.branchName ==  b)))
    RepositoryBranchesView(branches, repo.repository.repoData.repoType)
  }

}