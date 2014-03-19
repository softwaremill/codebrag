package com.softwaremill.codebrag.repository

import org.eclipse.jgit.revwalk.{RevWalk, RevCommit}
import com.softwaremill.codebrag.domain.{CommitsForBranch, MultibranchLoadCommitsResult}
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.errors.MissingObjectException
import scala.collection.JavaConversions._

trait RepositoryDeltaLoader extends RawCommitsConverter with BranchListModeSelector {

  self: Repository =>

  def getCommitsForBranch(branchName: String, lastKnownSHA: Option[String]): List[RevCommit] = {
    val branch = repo.resolve(branchName)
    val walker = new RevWalk(repo)
    setRangeStart(walker, branch)
    setRangeEnd(walker, lastKnownSHA)
    val commits = walker.iterator().toList
    walker.dispose
    logger.debug(s"Got ${commits.size} new commit(s) for branch ${branchName}")
    commits
  }

  def loadCommitsSince(lastKnownBranchPointers: Map[String, String]): MultibranchLoadCommitsResult = {
    val gitRepo = new Git(repo)
    val commitsForBranches = remoteBranches(gitRepo).map { branchName =>
        val rawCommits = getCommitsForBranch(branchName, lastKnownBranchPointers.get(branchName))
        val commitInfos = toCommitInfos(rawCommits)
        CommitsForBranch(branchName, commitInfos, repo.resolve(branchName))
      }
    MultibranchLoadCommitsResult(repoName, commitsForBranches)
  }

  private def remoteBranches(gitRepo: Git): List[String] = {
    gitRepo.branchList().setListMode(branchListMode).call().toList.map(_.getName).filterNot(_ == "refs/remotes/origin/HEAD")
  }

  private def setRangeStart(walker: RevWalk, startingCommit: ObjectId) {
    walker.markStart(walker.parseCommit(startingCommit))
  }

  private def setRangeEnd(walker: RevWalk, lastKnownCommitSHA: Option[String]) {
    lastKnownCommitSHA.foreach { sha =>
      try {
        val lastKnownCommit = repo.resolve(sha)
        walker.markUninteresting(walker.parseCommit(lastKnownCommit))
      } catch {
        case e: MissingObjectException => throw new RuntimeException(s"Cannot find commit with ID $sha", e)
      }
    }
  }

}