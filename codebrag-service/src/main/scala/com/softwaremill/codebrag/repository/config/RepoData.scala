package com.softwaremill.codebrag.repository.config

case class RepoData(repoLocation: String, repoName: String, repoType: String, repoCredentials: Option[RepoCredentials])
