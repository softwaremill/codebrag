package com.softwaremill.codebrag.dao.repositorystatus

import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.RepositoryStatus
import org.scalatest.FlatSpec
import com.softwaremill.codebrag.test.{ClearSQLDataAfterTest, FlatSpecWithSQL, ClearMongoDataAfterTest, FlatSpecWithMongo}

trait RepositoryStatusDAOSpec extends FlatSpec with ShouldMatchers {
  
  def repositoryStatusDAO: RepositoryStatusDAO

  it should "create repo-ready status with ready value" in {
    // given
    val repoName = "codebrag"
    val repoStatus = RepositoryStatus.ready(repoName)

    // when
    repositoryStatusDAO.updateRepoStatus(repoStatus)

    // then
    val Some(storedStatus) = repositoryStatusDAO.getRepoStatus(repoName)
    storedStatus.ready should be(true)
  }

  it should "update repo-ready status to ready and keep head id not changed" in {
    // given
    val repoName = "codebrag"
    repositoryStatusDAO.updateRepoStatus(RepositoryStatus.ready(repoName))

    // when
    val newRepoStatus = RepositoryStatus.ready(repoName)
    repositoryStatusDAO.updateRepoStatus(newRepoStatus)

    // then
    val Some(storedStatus) = repositoryStatusDAO.getRepoStatus(repoName)
    storedStatus.ready should be(true)
  }

  it should "update repo-ready status to not ready without error" in {
    // given
    val repoName = "codebrag"

    // when
    val repoStatus = RepositoryStatus.notReady(repoName)
    repositoryStatusDAO.updateRepoStatus(repoStatus)

    // then
    val Some(storedStatus) = repositoryStatusDAO.getRepoStatus(repoName)
    storedStatus.ready should be(false)
  }


  it should "update repo-ready status to not ready with error" in {
    // given
    val repoName = "codebrag"
    val errorMsg: String = "some error message"
    val repoStatus = RepositoryStatus.notReady(repoName, Some(errorMsg))

    // when
    repositoryStatusDAO.updateRepoStatus(repoStatus)

    // then
    val Some(storedStatus) = repositoryStatusDAO.getRepoStatus(repoName)
    storedStatus.ready should be(false)
    storedStatus.error should be(Some(errorMsg))
  }

}

class MongoRepositoryStatusDAOSpec extends FlatSpecWithMongo with ClearMongoDataAfterTest with RepositoryStatusDAOSpec {
  val repositoryStatusDAO = new MongoRepositoryStatusDAO()
}

class SQLRepositoryStatusDAOSpec extends FlatSpecWithSQL with ClearSQLDataAfterTest with RepositoryStatusDAOSpec {
  val repositoryStatusDAO = new SQLRepositoryStatusDAO(sqlDatabase)
}
