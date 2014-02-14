package com.softwaremill.codebrag.service.commits

import org.scalatest.{FlatSpec, BeforeAndAfter}
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.{CommitsUpdatedEvent, CommitInfo}
import com.softwaremill.codebrag.service.events.MockEventBus
import com.softwaremill.codebrag.common.ClockSpec
import com.typesafe.config.ConfigFactory
import java.util
import org.mockito.Mockito._
import org.mockito.Matchers._
import com.softwaremill.codebrag.domain.builder.CommitInfoAssembler
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.service.config.RepositoryConfig
import com.softwaremill.codebrag.repository.config.RepoData

class CommitImportServiceSpec extends FlatSpec with MockitoSugar with BeforeAndAfter with ShouldMatchers with MockEventBus with ClockSpec {

  var commitsLoader: CommitsLoader = _
  var commitInfoDao: CommitInfoDAO = _
  var service: CommitImportService = _

  val repoOwner = "johndoe"
  val repoName = "project"
  val EmptyCommitsList = List[CommitInfo]()

  val repoConfig = new RepoData("/tmp/repo", "my-repo", "git", repoCredentials = None)

  before {
    eventBus.clear()
    commitsLoader = mock[CommitsLoader]
    commitInfoDao = mock[CommitInfoDAO]
    service = new CommitImportService(commitsLoader, commitInfoDao, eventBus)
  }

  it should "not store anything when no new commits available" in {
    // given
    when(commitsLoader.loadNewCommits(repoConfig)).thenReturn(EmptyCommitsList)

    // when
    service.importRepoCommits(repoConfig)

    // then
    verify(commitInfoDao, never).storeCommit(any[CommitInfo])
  }


  it should "store all new commits available" in {
    val commits = freshCommits(5)
    when(commitsLoader.loadNewCommits(repoConfig)).thenReturn(commits)

    service.importRepoCommits(repoConfig)

    commits.foreach(commit => {
      verify(commitInfoDao).storeCommit(commit)
    })
  }

  it should "publish event with correct data about imported commits" in {
    // given
    val newCommits = freshCommits(2)
    when(commitsLoader.loadNewCommits(repoConfig)).thenReturn(newCommits)

    // when
    service.importRepoCommits(repoConfig)

    // then
    eventBus.size() should equal(1)
    val onlyEvent = getEvents(0).asInstanceOf[CommitsUpdatedEvent]
    onlyEvent.newCommits.size should equal(2)
    onlyEvent.newCommits(0).id should equal(newCommits(0).id)
    onlyEvent.newCommits(1).id should equal(newCommits(1).id)
    onlyEvent.newCommits(0).authorName should equal(newCommits(0).authorName)
    onlyEvent.newCommits(1).authorName should equal(newCommits(1).authorName)
  }

  it should "not publish event about updated commits when nothing gets updated" in {
    // given
    when(commitsLoader.loadNewCommits(repoConfig)).thenReturn(EmptyCommitsList)

    // when
    service.importRepoCommits(repoConfig)

    // then
    eventBus.size() should equal(0)
  }

  it should "publish event about first update if no commits found in dao" in {
    // given
    val newCommits = freshCommits(2)
    when(commitsLoader.loadNewCommits(repoConfig)).thenReturn(newCommits)
    when(commitInfoDao.hasCommits).thenReturn(false)
    // when
    service.importRepoCommits(repoConfig)

    // then
    val onlyEvent = getEvents(0).asInstanceOf[CommitsUpdatedEvent]
    onlyEvent.firstTime should equal(true)
  }

  it should "publish event about not-first update if some commits found in dao" in {
    // given
    val newCommits = freshCommits(2)
    when(commitsLoader.loadNewCommits(repoConfig)).thenReturn(newCommits)
    when(commitInfoDao.hasCommits).thenReturn(true)
    // when
    service.importRepoCommits(repoConfig)

    // then
    val onlyEvent = getEvents(0).asInstanceOf[CommitsUpdatedEvent]
    onlyEvent.firstTime should equal(false)
  }

  def freshCommits(commitsNumber: Int) = {
    (1 to commitsNumber).map( num => {
      CommitInfoAssembler.randomCommit.withId(ObjectId.massageToObjectId(num)).get
    }).toList
  }

}