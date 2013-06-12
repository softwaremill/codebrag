package com.softwaremill.codebrag.service.github

trait GitHubCommitImportServiceFactory {
  def createInstance(login: String): GitHubCommitImportService
}
