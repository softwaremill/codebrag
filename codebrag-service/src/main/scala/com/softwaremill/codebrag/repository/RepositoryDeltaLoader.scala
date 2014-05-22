package com.softwaremill.codebrag.repository

import org.eclipse.jgit.revwalk.{RevWalk, RevCommit}
import com.softwaremill.codebrag.domain.{CommitsForBranch, MultibranchLoadCommitsResult}
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.errors.MissingObjectException
import scala.collection.JavaConversions._
import org.eclipse.jgit.revwalk.filter.MaxCountRevFilter

trait RepositoryDeltaLoader extends RawCommitsConverter {

  self: Repository =>

  def getCommitsForBranch(branchName: String, lastKnownSHA: Option[String], maxCommitsForNewBranch: Int): List[RevCommit] = {
    val branch = repo.resolve(branchName)
    val walker = new RevWalk(repo)
    setRangeStart(walker, branch)
    setRangeEnd(walker, lastKnownSHA, maxCommitsForNewBranch)
    val commits = walker.iterator().toList
    walker.dispose
    logger.debug(s"Got ${commits.size} new commit(s) for branch ${branchName}")
    commits
  }

  def loadCommitsSince(lastKnownBranchPointers: Map[String, String], maxCommitsForNewBranch: Int): MultibranchLoadCommitsResult = {
    val commitsForBranches = remoteBranchesFullNames.map { branchName =>
        val rawCommits = getCommitsForBranch(branchName, lastKnownBranchPointers.get(branchName), maxCommitsForNewBranch)
        val commitInfos = toCommitInfos(rawCommits)
        CommitsForBranch(branchName, commitInfos, gitBranchRefToString(repo.resolve(branchName)))
      }
    MultibranchLoadCommitsResult(repoName, commitsForBranches.toList)
  }

  private def setRangeStart(walker: RevWalk, startingCommit: ObjectId) {
    walker.markStart(walker.parseCommit(startingCommit))
  }

  private def setRangeEnd(walker: RevWalk, lastKnownCommitSHA: Option[String], maxCommitsForNewBranch: Int) {
    lastKnownCommitSHA match {
      case Some(sha) => {
        try {
          val lastKnownCommit = repo.resolve(sha)
          walker.markUninteresting(walker.parseCommit(lastKnownCommit))
        } catch {
          case e: MissingObjectException => throw new RuntimeException(s"Cannot find commit with ID $sha", e)
        }
      }
      case None => walker.setRevFilter(MaxCountRevFilter.create(maxCommitsForNewBranch))
    }
  }

}