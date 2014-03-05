package com.softwaremill.codebrag.repository

import com.typesafe.scalalogging.slf4j.Logging
import org.eclipse.jgit.lib.{ObjectId, Constants}
import org.eclipse.jgit.revwalk.{RevWalk, RevCommit}
import org.eclipse.jgit.storage.file.{FileRepository, FileRepositoryBuilder}
import java.io.File
import org.eclipse.jgit.errors.MissingObjectException
import com.softwaremill.codebrag.repository.config.RepoData
import scala.collection.JavaConversions._
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ListBranchCommand.ListMode
import com.softwaremill.codebrag.domain.{CommitsForBranch, MultibranchLoadCommitsResult}
import com.softwaremill.codebrag.service.commits.jgit.RawCommitsConverter
import org.eclipse.jgit.revwalk.filter.MaxCountRevFilter

trait Repository extends Logging with RawCommitsConverter {

  def repoData: RepoData
  val repo = buildRepository
  val repoName = repoData.repoName

  def pullChanges() {
    logger.debug(s"Pulling changes for ${repoData.repoLocation}")
    try {
      pullChangesForRepo()
      logger.debug(s"Changes pulled succesfully")
    } catch {
      case e: Exception => {
        logger.error(s"Cannot pull changes for repo ${repoData.repoLocation} because of: ${e.getMessage}")
        throw e
      }
    }
  }

  def currentHead = {
    repo.resolve(Constants.HEAD)
  }

  def getCommit(sha: String): Option[RevCommit] = {
    Option(repo.resolve(sha)).flatMap { commitId =>
      try {
        Some(new RevWalk(repo).parseCommit(commitId))
      } catch {
        case e: MissingObjectException => None
      }
    }
  }

  def getCommits(lastKnownCommitSHA: Option[String] = None): List[RevCommit] = {
    getCommitsForBranch("refs/heads/master", lastKnownCommitSHA)
  }
  
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

  private implicit def gitObjectIdToString(objId: ObjectId) = ObjectId.toString(objId)

  def loadSnapshot(lastKnownBranchPointers: Map[String, String], perBranchMaxCommitsCount: Int): MultibranchLoadCommitsResult = {
    val commonBranchPointers = CommonBranchesResolver.rejectNonExistingBranches(lastKnownBranchPointers, repo)
    val commitsForBranches = commonBranchPointers.map { case (branchName, branchKnownTop) =>
      val rawCommits = getOldBranchCommitsUntil(branchName, branchKnownTop, perBranchMaxCommitsCount)
      val commitInfos = toPartialCommitInfos(rawCommits, repo)
      CommitsForBranch(branchName, commitInfos, repo.resolve(branchName))
    }.toList
    MultibranchLoadCommitsResult(repoName, commitsForBranches)
  }

  private def getOldBranchCommitsUntil(branchName: String, untilSHA: String, commitsCount: Int): List[RevCommit] = {
    val walker = new RevWalk(repo)
    walker.setRevFilter(MaxCountRevFilter.create(commitsCount))
    setRangeStart(walker, repo.resolve(untilSHA))
    val commits = walker.iterator().toList
    walker.dispose
    logger.debug(s"Got ${commits.size} old commit(s) for branch ${branchName}")
    commits
  }

  protected def pullChangesForRepo()

  private def buildRepository = {
    try {
      new FileRepositoryBuilder().setGitDir(new File(repoData.repoLocation + File.separator + ".git")).setMustExist(true).build()
    } catch {
      case e: Exception => throw new RuntimeException(s"Cannot build valid git repository object from ${repoData.repoLocation}", e)
    }
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


object CommonBranchesResolver {
  def rejectNonExistingBranches(knownRepoSnapshot: Map[String, String], repo: FileRepository) = {
    val gitRepo = new Git(repo)
    val repoBranches = gitRepo.branchList().setListMode(ListMode.ALL).call().toList.map(_.getName)
    val commonBranches = repoBranches.intersect(knownRepoSnapshot.keySet.toList)
    knownRepoSnapshot.filter { b => commonBranches.contains(b._1) }
  }
}
