package com.softwaremill.codebrag.dao.reporting

/**
 * Responsible for fetching commit list in read model.
 */
trait CommitListFinder {

  def findAllPendingCommits(): CommitListDTO
}
