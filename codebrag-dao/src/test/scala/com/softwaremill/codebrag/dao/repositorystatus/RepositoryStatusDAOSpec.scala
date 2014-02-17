package com.softwaremill.codebrag.dao.repositorystatus

import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.RepositoryStatus
import org.scalatest.FlatSpec
import com.softwaremill.codebrag.test.{ClearSQLDataAfterTest, FlatSpecWithSQL, ClearMongoDataAfterTest, FlatSpecWithMongo}

trait RepositoryStatusDAOSpec extends FlatSpec with ShouldMatchers {
  def repositoryStatusDAO: RepositoryStatusDAO

  it should "store repository HEAD reference if no record for given repo exists" in {
    // given
    val repoName = "codebrag"
    val newHeadId = "123abc"

    // when
    repositoryStatusDAO.updateRepoStatus(RepositoryStatus.ready(repoName).withHeadId(newHeadId))
    val Some(fetched) = repositoryStatusDAO.getRepoStatus(repoName)

    // then
    fetched.headId should be(Some(newHeadId))
  }

  it should "update repository HEAD reference if record for given repo already exists" in {
    // given
    val repoName = "codebrag"
    val oldHeadId = "123abc"
    val newHeadId = "456def"
    repositoryStatusDAO.updateRepoStatus(RepositoryStatus.ready(repoName).withHeadId(oldHeadId))

    // when
    repositoryStatusDAO.updateRepoStatus(RepositoryStatus.ready(repoName).withHeadId(newHeadId))
    val Some(fetched) = repositoryStatusDAO.getRepoStatus(repoName)

    // then
    fetched.headId should be(Some(newHeadId))
  }

  it should "update have none as a result when no HEAD ref for given repo exists" in {
    // given
    val repoName = "codebrag"

    // when
    val fetched = repositoryStatusDAO.getRepoStatus(repoName)

    // then
    fetched should be(None)
  }

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
    repositoryStatusDAO.update(repoName, "123123")
    val repoStatus = RepositoryStatus.ready(repoName)

    // when
    repositoryStatusDAO.updateRepoStatus(repoStatus)

    // then
    val Some(storedStatus) = repositoryStatusDAO.getRepoStatus(repoName)
    storedStatus.ready should be(true)
    storedStatus.headId should be(Some("123123"))
  }

  it should "update repo-ready status to not ready without error" in {
    // given
    val repoName = "codebrag"
    val repoStatus = RepositoryStatus.notReady(repoName)

    // when
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
    storedStatus.headId should be(None)
  }

  it should "update head id and keep repo status unchanged" in {
    // given
    val repoName = "codebrag"
    val repoStatus = RepositoryStatus.ready(repoName)
    repositoryStatusDAO.updateRepoStatus(repoStatus)

    // when
    repositoryStatusDAO.update(repoName, "123123")

    // then
    val Some(storedStatus) = repositoryStatusDAO.getRepoStatus(repoName)
    storedStatus.ready should be(true)
    storedStatus.headId should be(Some("123123"))
  }
}

class MongoRepositoryStatusDAOSpec extends FlatSpecWithMongo with ClearMongoDataAfterTest with RepositoryStatusDAOSpec {
  val repositoryStatusDAO = new MongoRepositoryStatusDAO()
}

class SQLRepositoryStatusDAOSpec extends FlatSpecWithSQL with ClearSQLDataAfterTest with RepositoryStatusDAOSpec {
  val repositoryStatusDAO = new SQLRepositoryStatusDAO(sqlDatabase)
}
