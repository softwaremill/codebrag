package com.softwaremill.codebrag.repository

import org.eclipse.jgit.api.ListBranchCommand.ListMode

trait BranchListModeSelector {

  def branchListMode = ListMode.REMOTE

}
