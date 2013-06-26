package com.softwaremill.codebrag.service.commits

trait GitHubCommitImportServiceFactory {
  def fetchToken(login: String): String
  def createInstance(login: String): CommitImportService
}
