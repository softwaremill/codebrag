package com.softwaremill.codebrag.service.commits

trait GitHubCommitImportServiceFactory {
  def createInstance(login: String): CommitImportService
}
