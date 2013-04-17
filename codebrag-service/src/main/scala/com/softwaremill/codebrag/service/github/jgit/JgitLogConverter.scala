package com.softwaremill.codebrag.service.github.jgit

import org.eclipse.jgit.revwalk.RevCommit
import com.softwaremill.codebrag.domain.CommitInfo

class JgitLogConverter {

  def toCommitInfos(jGitCommits: List[RevCommit]): List[CommitInfo] = {
    List.empty
  }
}
