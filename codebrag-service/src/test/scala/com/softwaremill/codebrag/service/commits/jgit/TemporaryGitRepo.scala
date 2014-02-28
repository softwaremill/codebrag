package com.softwaremill.codebrag.service.commits.jgit

import com.google.common.io.Files
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.lib.{ObjectId, Repository}
import com.softwaremill.codebrag.common.Utils
import java.io.{PrintWriter, File}
import org.eclipse.jgit.api.Git

class TemporaryGitRepo(val tempDir: File, repo: Repository) {
  private val git = new Git(repo)

  /**
   * @return SHA of the commit
   */
  def createCommit(commitMessage: String, fileNamesAndContents: (String, String)*): String = {
    fileNamesAndContents.foreach { case (fileName, content) =>
      val file = new File(tempDir, fileName)
      setFileContent(file, content)
      git.add().addFilepattern(fileName).call()
    }

    ObjectId.toString(git.commit().setMessage(commitMessage).call().getId)
  }

  def checkoutBranch(branchName: String) {
    git.checkout().setName(branchName).setCreateBranch(true).call()
  }

  private def setFileContent(file: File, content: String) {
    val p = new PrintWriter(file)
    try {
      p.println(content)
    } finally {
      p.close()
    }
  }
}

object TemporaryGitRepo {
  def withGitRepo[T](block: TemporaryGitRepo => T) = {
    val tempDir = Files.createTempDir()

    try {
      val repo = new FileRepositoryBuilder().setWorkTree(tempDir).build()
      repo.create(false)

      block(new TemporaryGitRepo(tempDir, repo))
    } finally {
      Utils.rmMinusRf(tempDir)
    }
  }
}
