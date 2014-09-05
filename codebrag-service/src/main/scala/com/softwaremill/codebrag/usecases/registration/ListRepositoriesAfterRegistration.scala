package com.softwaremill.codebrag.usecases.registration

import com.softwaremill.codebrag.cache.RepositoriesCache
import com.softwaremill.codebrag.service.invitations.InvitationService

class ListRepositoriesAfterRegistration(repositoriesCache: RepositoriesCache, val invitationService: InvitationService) extends ValidateInvitationCode {

  type RepoNamesList = Seq[String]

  def execute(invitationCode: String) = {
    validateCode(invitationCode).whenOk[RepoNamesList] {
      repositoriesCache.repoNames
    }
  }

}