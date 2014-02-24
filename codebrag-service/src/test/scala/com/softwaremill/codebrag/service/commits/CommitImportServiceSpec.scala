package com.softwaremill.codebrag.service.commits

import org.scalatest.{FlatSpec, BeforeAndAfter}
import org.scalatest.mock.MockitoSugar
import com.softwaremill.codebrag.dao.CommitInfoDAO
import org.scalatest.matchers.ShouldMatchers
import org.mockito.Mockito._
import org.mockito.Matchers._
import org.mockito.BDDMockito._
import com.softwaremill.codebrag.domain.{CommitsUpdatedEvent, CommitInfo}
import com.softwaremill.codebrag.domain.builder.CommitInfoAssembler
import org.bson.types.ObjectId
import com.softwaremill.codebrag.service.events.MockEventBus
import com.softwaremill.codebrag.common.ClockSpec

class CommitImportServiceSpec
  extends FlatSpec with MockitoSugar with BeforeAndAfter with ShouldMatchers with MockEventBus with ClockSpec {

  var commitsLoader: CommitsLoader = _
  var commitInfoDao: CommitInfoDAO = _
  var service: CommitImportService = _

  val repoOwner = "johndoe"
  val repoName = "project"
  val EmptyCommitsList = List[CommitInfo]()

  val mockRepoData = mock[RepoData]

  before {
    eventBus.clear()
    commitsLoader = mock[CommitsLoader]
    commitInfoDao = mock[CommitInfoDAO]
    service = new CommitImportService(commitsLoader, commitInfoDao, eventBus)
  }

  it should "not store anything when no new commits available" in {
    when(commitsLoader.loadMissingCommits(mockRepoData)).thenReturn(EmptyCommitsList)

    service.importRepoCommits(mockRepoData)

    verify(commitInfoDao, never).storeCommit(any[CommitInfo])
  }

  it should "store all new commits available" in {
    val newCommits = newGithubCommits(5)
    when(commitsLoader.loadMissingCommits(mockRepoData)).thenReturn(newCommits)

    service.importRepoCommits(mockRepoData)

    newCommits.foreach(commit => {
      verify(commitInfoDao).storeCommit(commit)
    })
  }

  it should "skip commits that could not be saved and move on" in {
    val newCommits = newGithubCommits(5)
    when(commitsLoader.loadMissingCommits(mockRepoData)).thenReturn(newCommits)
    when(commitInfoDao.storeCommit(any[CommitInfo])).thenThrow(new RuntimeException("Could not store commit"))

    service.importRepoCommits(mockRepoData)

    newCommits.foreach(commit => {
      verify(commitInfoDao).storeCommit(commit)
    })
  }

  it should "publish event with correct data about imported commits" in {
    // given
    val newCommits = newGithubCommits(2)
    given(commitsLoader.loadMissingCommits(mockRepoData)).willReturn(newCommits)

    // when
    service.importRepoCommits(mockRepoData)

    // then
    eventBus.size() should equal(1)
    val onlyEvent = getEvents(0).asInstanceOf[CommitsUpdatedEvent]
    onlyEvent.newCommits.size should equal(2)
    onlyEvent.newCommits(0).id should equal(newCommits(0).id)
    onlyEvent.newCommits(1).id should equal(newCommits(1).id)
    onlyEvent.newCommits(0).authorName should equal(newCommits(0).authorName)
    onlyEvent.newCommits(1).authorName should equal(newCommits(1).authorName)
  }

  it should "publish event with commits that were really saved (not ones that were imported only)" in {
    // given
    val invalidCommit = CommitInfoAssembler.randomCommit.withMessage("Invalid").get
    val validCommit = CommitInfoAssembler.randomCommit.withMessage("Valid").get
    given(commitsLoader.loadMissingCommits(mockRepoData)).willReturn(List(invalidCommit, validCommit))
    when(commitInfoDao.storeCommit(invalidCommit)).thenThrow(new RuntimeException("Cannot store commit"))

    // when
    service.importRepoCommits(mockRepoData)

    val event = getEvents(0).asInstanceOf[CommitsUpdatedEvent]
    val commit = event.newCommits.head
    commit.id should equal(validCommit.id)
    commit.authorName should equal(validCommit.authorName)
  }

  it should "not publish event about updated commits when nothing gets updated" in {
    // given
    given(commitsLoader.loadMissingCommits(mockRepoData)).willReturn(EmptyCommitsList)

    // when
    service.importRepoCommits(mockRepoData)

    // then
    eventBus.size() should equal(0)
  }

  it should "publish event about first update if no commits found in dao" in {
    // given
    val newCommits = newGithubCommits(2)
    given(commitsLoader.loadMissingCommits(mockRepoData)).willReturn(newCommits)
    given(commitInfoDao.hasCommits).willReturn(false)
    // when
    service.importRepoCommits(mockRepoData)

    // then
    val onlyEvent = getEvents(0).asInstanceOf[CommitsUpdatedEvent]
    onlyEvent.firstTime should equal(true)
  }

  it should "publish event about not-first update if some commits found in dao" in {
    // given
    val newCommits = newGithubCommits(2)
    given(commitsLoader.loadMissingCommits(mockRepoData)).willReturn(newCommits)
    given(commitInfoDao.hasCommits).willReturn(true)
    // when
    service.importRepoCommits(mockRepoData)

    // then
    val onlyEvent = getEvents(0).asInstanceOf[CommitsUpdatedEvent]
    onlyEvent.firstTime should equal(false)
  }

  def newGithubCommits(commitsNumber: Int) = {
    (1 to commitsNumber).map( num => {
      CommitInfoAssembler.randomCommit.withId(ObjectId.massageToObjectId(num)).get
    }).toList
  }

}