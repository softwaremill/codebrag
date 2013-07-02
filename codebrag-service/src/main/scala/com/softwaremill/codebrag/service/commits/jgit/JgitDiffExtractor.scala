package com.softwaremill.codebrag.service.commits.jgit

import org.eclipse.jgit.revwalk.{RevTree, RevWalk, RevCommit}
import org.eclipse.jgit.lib.{Constants, ObjectId, Repository}
import org.eclipse.jgit.diff.{DiffFormatter, RawTextComparator, DiffEntry}
import java.io.ByteArrayOutputStream
import scala.collection.JavaConversions._
import com.softwaremill.codebrag.domain.CommitFileInfo
import org.eclipse.jgit.diff.DiffEntry.ChangeType

trait JgitDiffExtractor {
  def extractDiffsFromCommit(commit: RevCommit, repository: Repository): List[CommitFileInfo] = {

    val tree = commit.getTree
    val baseTree = if (isInitial(commit))
      emptyTree(repository)
    else
      firstParent(repository, commit)

    val outputStream = new ByteArrayOutputStream
    val formatter = new DiffFormatter(outputStream)
    formatter.setRepository(repository)
    formatter.setDiffComparator(RawTextComparator.WS_IGNORE_CHANGE)
    formatter.setDetectRenames(true)

    try {
      val entries = formatter.scan(baseTree, tree).toList
      for (diffEntry: DiffEntry <- entries) yield {

        formatter.format(diffEntry)
        val patch = outputStream.toString
        outputStream.reset()
        val filename = diffEntry.getChangeType match {
          case ChangeType.ADD => diffEntry.getNewPath
          case _ => diffEntry.getOldPath
        }
        val status = changeTypeToStatus(diffEntry.getChangeType)
        CommitFileInfo(filename, status, patch)
      }
    }
    finally {
      formatter.release()
    }
  }


  private def firstParent(repository: Repository, commit: RevCommit): RevTree = {
    val walk = new RevWalk(repository)
    val parent = walk.parseCommit(commit.getParent(0).getId)
    walk.dispose()
    parent.getTree
  }

  private def isInitial(commit: RevCommit): Boolean = commit.getParentCount == 0


  private def changeTypeToStatus(change: ChangeType): String = {
    change match {
      case ChangeType.ADD => "added"
      case ChangeType.DELETE => "deleted"
      case ChangeType.MODIFY => "modified"
      case ChangeType.COPY => "copied"
      case ChangeType.RENAME => "renamed"
      case _ => "unknown"
    }
  }

  private def emptyTree(repo: Repository): ObjectId = {
    val inserter = repo.newObjectInserter
    try {
      val newTreeId = inserter.insert(Constants.OBJ_TREE, Array[Byte]())
      inserter.flush()
      newTreeId
    }
    finally {
      inserter.release()
    }
  }
}
