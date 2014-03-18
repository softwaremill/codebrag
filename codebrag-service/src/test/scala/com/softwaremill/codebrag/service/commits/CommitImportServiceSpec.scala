package com.softwaremill.codebrag.service.commits

import org.scalatest.{FlatSpec, BeforeAndAfter}
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.{RepositoryStatus, MultibranchLoadCommitsResult}
import org.mockito.Mockito._
import com.softwaremill.codebrag.dao.repositorystatus.RepositoryStatusDAO
import com.softwaremill.codebrag.service.commits.branches.RepositoryCache
import com.softwaremill.codebrag.repository.Repository
import org.mockito.Matchers
import com.softwaremill.codebrag.dao.branchsnapshot.BranchStateDAO

class CommitImportServiceSpec extends FlatSpec with MockitoSugar with BeforeAndAfter with ShouldMatchers {

  var repoStatusDao: RepositoryStatusDAO = _
  var branchStateDao: BranchStateDAO = _
  var repoCache: RepositoryCache = _
  var repository: Repository = _
  var service: CommitImportService = _

  val SavedRepoState = Map.empty[String, String]
  val LoadedCommits = MultibranchLoadCommitsResult("test-repo", List.empty)

  before {
    repoStatusDao = mock[RepositoryStatusDAO]
    branchStateDao = mock[BranchStateDAO]
    repoCache = mock[RepositoryCache]
    repository = mock[Repository]
    service = new CommitImportService(repoStatusDao, branchStateDao, repoCache)
  }

  it should "pull changes and load commits from repo since given (saved) state" in {
    // given
    when(branchStateDao.loadBranchesStateAsMap).thenReturn(SavedRepoState)

    // when
    service.importRepoCommits(repository)

    // then
    verify(repository).pullChanges()
    verify(repository).loadCommitsSince(SavedRepoState)
  }
  
  it should "add loaded commits to cache" in {
    // given
    when(branchStateDao.loadBranchesStateAsMap).thenReturn(SavedRepoState)
    when(repository.loadCommitsSince(SavedRepoState)).thenReturn(LoadedCommits)
    
    // when
    service.importRepoCommits(repository)
    
    // then
    verify(repoCache).addCommits(LoadedCommits)
  }

  it should "update repo status to not-ready when commits import failed" in {
    // given
    when(repository.loadCommitsSince(Matchers.any[Map[String, String]])).thenThrow(new RuntimeException("oops"))

    // when
    service.importRepoCommits(repository)

    // then
    val expectedRepoStatus = RepositoryStatus.notReady(repository.repoName, Some("oops"))
    verify(repoStatusDao).updateRepoStatus(expectedRepoStatus)
  }

}