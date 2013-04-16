package com.softwaremill.codebrag.service.github

trait GitHubCommitImportServiceFactory {
  def createInstance(email: String): GitHubCommitImportService
}
