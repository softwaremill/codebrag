package com.softwaremill.codebrag.service.commits.svnkit

import org.tmatesoft.svn.core.wc.{SVNDiffClient, SVNWCUtil, SVNRevision, SVNClientManager}
import java.io.File
import org.tmatesoft.svn.core.{SVNLogEntry, ISVNLogEntryHandler, SVNDepth, SVNURL}
import com.softwaremill.codebrag.domain.{CommitInfo, CommitFileInfo}
import java.nio.file.{Path, Files}
import collection.JavaConversions._
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import scala.collection.mutable
import org.joda.time.DateTime
import com.softwaremill.codebrag.service.commits.{SvnRepoData, RepoData}
import com.typesafe.scalalogging.slf4j.Logging
import scala.Predef._
import com.softwaremill.codebrag.domain.CommitFileInfo
import com.softwaremill.codebrag.service.commits.SvnRepoData

class SvnFacade extends Logging {

  def update(checkoutDir: File, uri: String, repoData: SvnRepoData) : Long = {
    val clientManager = SVNClientManager.newInstance(null, repoData.credentials)

    val updateClient = clientManager.getUpdateClient

    updateClient.setIgnoreExternals(false)

    val svnUrl = SVNURL.parseURIDecoded(uri)

    if (SVNWCUtil.isWorkingCopyRoot(checkoutDir)) {
      updateClient.doUpdate(checkoutDir, SVNRevision.HEAD, SVNDepth.INFINITY, true, true)
    }
    else {
      updateClient.doCheckout(svnUrl, checkoutDir,
        SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY, true)
    }
  }

  def log(checkoutDir: File, previousHead: String, repoData: SvnRepoData): List[CommitInfo] = {
    val clientManager = SVNClientManager.newInstance(null, repoData.credentials)

    val logClient = clientManager.getLogClient
    val diffClient = SVNClientManager.newInstance(clientManager.getOptions, repoData.credentials).getDiffClient
    diffClient.setGitDiffFormat(true)

    val startRev = SVNRevision.parse(previousHead)

    val commits = new mutable.MutableList[CommitInfo]

    if (!currentWCRevision(clientManager, checkoutDir).equals(startRev)) {

      val svnUrl = clientManager.getWCClient.doInfo(checkoutDir, SVNRevision.HEAD).getRepositoryRootURL

      logClient.doLog(Array(checkoutDir), SVNRevision.UNDEFINED,
        // exclude the changes from last HEAD
        if (0l equals startRev.getNumber) startRev else SVNRevision.create(startRev.getNumber + 1),
        SVNRevision.HEAD,
        false, true, SvnFacade.MAX_COMMITS, new ISVNLogEntryHandler() {
          def handleLogEntry(logEntry: SVNLogEntry) {
            logger.debug(s"New log: ${logEntry.toString}")

            val files = new mutable.MutableList[CommitFileInfo]

            logEntry.getChangedPaths foreach {
              case (path, logEntryPath) => {

                val filePath = svnUrl.appendPath(logEntryPath.getPath, false)

                logger.debug(s"Getting diff for ${filePath.toString}")

                files += new CommitFileInfo(logEntryPath.getPath, "",
                  getDiffForPath(logEntry.getRevision, diffClient, filePath))
              }
            }

            commits +=
              CommitInfo(
                logEntry.getRevision.toString,
                logEntry.getMessage,
                logEntry.getAuthor, "",
                logEntry.getAuthor, "",
                new DateTime(logEntry.getDate),
                new DateTime(logEntry.getDate),
                List.empty,
                files.toList)
          }
        })

    }

    commits.toList
  }

  private def currentWCRevision(clientManager: SVNClientManager, checkoutDir: File) {
    clientManager.getWCClient.doInfo(checkoutDir, SVNRevision.HEAD).getRevision
  }

  private def getDiffForPath(revision: Long, diffClient : SVNDiffClient, path : SVNURL) = {
    val out = new ByteOutputStream()

    diffClient.doDiff(path, SVNRevision.create(revision - 1),
      path, SVNRevision.create(revision), SVNDepth.INFINITY, true, out)

    new String(out.getBytes)
  }
}

object SvnFacade {
  // read commits by 10 batches
  val MAX_COMMITS = 10
}