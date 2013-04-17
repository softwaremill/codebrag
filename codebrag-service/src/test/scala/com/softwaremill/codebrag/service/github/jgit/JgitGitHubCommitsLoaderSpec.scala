package com.softwaremill.codebrag.service.github.jgit

import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatest.matchers.ShouldMatchers

import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.BDDMockito._
import java.nio.file.Paths
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.api.{Git, LogCommand}
import java.util
import org.eclipse.jgit.revwalk.RevCommit
import com.softwaremill.codebrag.dao.CommitInfoBuilder

class JgitGitHubCommitsLoaderSpec extends FlatSpec with ShouldMatchers with MockitoSugar with BeforeAndAfter
with JgitGitHubCommitsLoaderSpecFixture {


  var jGitFacadeMock: JgitFacade = _
  var internalGitDirTreeMock: InternalGitDirTree = _
  var converterMock: JgitLogConverter = _
  var logCommandMock: LogCommand = _
  var gitMock: Git = _
  var loader: JgitGitHubCommitsLoader = _

  behavior of "JGit GitHub Commits Loader"

  before {
    jGitFacadeMock = mock[JgitFacade]
    internalGitDirTreeMock = mock[InternalGitDirTree]
    when(internalGitDirTreeMock.getPath("softwaremill", "codebrag")).thenReturn(InternalCodebragDir)
    converterMock = mock[JgitLogConverter]
    loader = new JgitGitHubCommitsLoader(jGitFacadeMock, internalGitDirTreeMock, converterMock)
    logCommandMock = mock[LogCommand]
    gitMock = mock[Git]
  }

  it should "clone if local repo doesn't exist" in {
    // given
    given(internalGitDirTreeMock.containsRepo("softwaremill", "codebrag")).willReturn(false)
    given(jGitFacadeMock.clone(RemoteUri, InternalCodebragDir)).willReturn(gitMock)
    given(gitMock.log()).willReturn(logCommandMock)
    given(logCommandMock.call()).willReturn(new util.ArrayList[RevCommit]())

    // when
    loader.loadMissingCommits("softwaremill", "codebrag")

    // then

    verify(jGitFacadeMock).clone(RemoteUri, InternalCodebragDir)
  }

  it should "pull if local repo already exists" in {
    // given
    given(jGitFacadeMock.getHeadId(InternalCodebragDir)).willReturn(Sha1)
    given(internalGitDirTreeMock.containsRepo("softwaremill", "codebrag")).willReturn(true)
    given(jGitFacadeMock.pull(InternalCodebragDir)).willReturn(gitMock)
    given(gitMock.log()).willReturn(logCommandMock)
    given(logCommandMock.addRange(Sha1, Sha2)).willReturn(logCommandMock)
    given(logCommandMock.call()).willReturn(new util.ArrayList[RevCommit]())

    // when
    loader.loadMissingCommits("softwaremill", "codebrag")

    // then
    verify(jGitFacadeMock).pull(InternalCodebragDir)
  }

  it should "call git log with proper sha range" in {
    // given
    given(jGitFacadeMock.getHeadId(InternalCodebragDir)).willReturn(Sha1).willReturn(Sha2)
    given(internalGitDirTreeMock.containsRepo("softwaremill", "codebrag")).willReturn(true)
    given(jGitFacadeMock.pull(InternalCodebragDir)).willReturn(gitMock)
    given(gitMock.log()).willReturn(logCommandMock)
    given(logCommandMock.addRange(Sha1, Sha2)).willReturn(logCommandMock)
    given(logCommandMock.call()).willReturn(new util.ArrayList[RevCommit]())

    // when
    loader.loadMissingCommits("softwaremill", "codebrag")

    // then
    verify(jGitFacadeMock).pull(InternalCodebragDir)
    verify(logCommandMock).addRange(Sha1, Sha2)
  }

  it should "call converter to build commit infos" in {
    // given
    given(jGitFacadeMock.getHeadId(InternalCodebragDir)).willReturn(Sha1).willReturn(Sha2)
    given(internalGitDirTreeMock.containsRepo("softwaremill", "codebrag")).willReturn(true)
    given(jGitFacadeMock.pull(InternalCodebragDir)).willReturn(gitMock)
    given(gitMock.log()).willReturn(logCommandMock)
    given(logCommandMock.addRange(Sha1, Sha2)).willReturn(logCommandMock)
    given(logCommandMock.call()).willReturn(util.Arrays.asList[RevCommit](RevCommit1, RevCommit2))
    given(converterMock.toCommitInfos(List(RevCommit1, RevCommit2)))
      .willReturn(List(CommitInfo1, CommitInfo2))

    // when
    val result = loader.loadMissingCommits("softwaremill", "codebrag")

    // then
    verify(jGitFacadeMock).pull(InternalCodebragDir)
    verify(logCommandMock).addRange(Sha1, Sha2)
    result should equal (List(CommitInfo1, CommitInfo2))
  }
}

trait JgitGitHubCommitsLoaderSpecFixture extends MockitoSugar {
  val InternalCodebragDir = Paths.get(s"${InternalGitDirTree.Root}/softwaremill/codebrag")
  val RemoteUri = "https://github.com/softwaremill/codebrag.git"
  val RevCommit1 = mock[RevCommit]
  val RevCommit2 = mock[RevCommit]
  val Sha1: ObjectId = mock[ObjectId]
  val Sha2: ObjectId = mock[ObjectId]
  val CommitInfo1 = CommitInfoBuilder.createRandomCommit()
  val CommitInfo2 = CommitInfoBuilder.createRandomCommit()
}