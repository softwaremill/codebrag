package com.softwaremill.codebrag.service.commits.jgit

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.repository.config.RepoData
import java.io.{PrintWriter, File}

class JgitDiffLoaderSpec extends FlatSpec with ShouldMatchers {
  it should "get the diff of the given commit" in {
    TemporaryGitRepo.withGitRepo { gitRepo =>
      // given
      gitRepo.createCommit("test1", ("test.txt", "AAA\nBBB\nCCC\nDDD\nEEE"))
      val sha2 = gitRepo.createCommit("test1", ("test.txt", "AAA\nBBB\nccc\nDDD\nEEE"))
      gitRepo.createCommit("test1", ("test.txt", "AAA\nBBB\nxxx\nDDD\nEEE"))

      // when
      val filesOpt = new JgitDiffLoader().loadDiff(sha2,
        RepoData(gitRepo.tempDir.getAbsolutePath, "temp", "git", None))

      // then
      filesOpt should be ('defined)

      val files = filesOpt.get
      files should have size (1)

      val file = files.head
      file.patch should be ("""diff --git a/test.txt b/test.txt
                              |index 988cecb..a109d84 100644
                              |--- a/test.txt
                              |+++ b/test.txt
                              |@@ -1,5 +1,5 @@
                              | AAA
                              | BBB
                              |-CCC
                              |+ccc
                              | DDD
                              | EEE
                              |""".stripMargin)
    }
  }

  it should "return None for a non-existing commit" in {
    TemporaryGitRepo.withGitRepo { gitRepo =>
      // given
      gitRepo.createCommit("test1", ("test.txt", "AAA\nBBB\nCCC\nDDD\nEEE"))

      // when
      val filesOpt = new JgitDiffLoader().loadDiff("5876164cee034bf50ef2424d4d1e67300385b1e7", // another SHA
        RepoData(gitRepo.tempDir.getAbsolutePath, "temp", "git", None))

      // then
      filesOpt should be (None)
    }
  }

  def setFileContent(file: File, content: String) {
    val p = new PrintWriter(file)
    try {
      p.println(content)
    } finally {
      p.close()
    }
  }
}
