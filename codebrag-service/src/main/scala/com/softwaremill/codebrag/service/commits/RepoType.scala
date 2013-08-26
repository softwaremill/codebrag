package com.softwaremill.codebrag.service.commits

sealed abstract class RepoType(val configRepoType: String)

case object GithubRepoType extends RepoType("github")
case object GitHttpsRepoType extends RepoType("git-https")
case object GitSshRepoType extends RepoType("git-ssh")
case object SvnRepoType extends RepoType("svn")
