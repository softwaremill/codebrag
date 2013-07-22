package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.test.mongo.ClearDataAfterTest
import org.scalatest.matchers.ShouldMatchers

class MongoRepositoryHeadStoreSpec extends FlatSpecWithMongo with ClearDataAfterTest with ShouldMatchers {

  var repoHeadDao = new MongoRepositoryHeadStore

  it should "store repository HEAD reference if no record for given repo exists" in {
    // given
    val repoName = "codebrag"
    val newHeadId = "123abc"

    // when
    repoHeadDao.update(repoName, newHeadId)
    val Some(fetched) = repoHeadDao.get(repoName)

    // then
    fetched should be(newHeadId)
  }

  it should "update repository HEAD reference if record for given repo already exists" in {
    // given
    val repoName = "codebrag"
    val oldHeadId = "123abc"
    val newHeadId = "456def"
    repoHeadDao.update(repoName, oldHeadId)

    // when
    repoHeadDao.update(repoName, newHeadId)
    val Some(fetched) = repoHeadDao.get(repoName)

    // then
    fetched should be(newHeadId)
  }

  it should "update have none as a result when no HEAD ref for given repo exists" in {
    // given
    val repoName = "codebrag"

    // when
    repoHeadDao.get(repoName)
    val fetched = repoHeadDao.get(repoName)

    // then
    fetched should be(None)
  }

}
