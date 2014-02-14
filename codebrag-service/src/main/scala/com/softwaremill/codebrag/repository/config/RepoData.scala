package com.softwaremill.codebrag.repository.config

import com.softwaremill.codebrag.repository.{GitSvnRepository, GitRepository}

case class RepoData(repoLocation: String, repoName: String, repoType: String, repoCredentials: Option[RepoCredentials]) {

  def buildRepository = {
    repoType match {
      case "git" => new GitRepository(this)
      case "git-svn" => new GitSvnRepository(this)
    }
  }

}
