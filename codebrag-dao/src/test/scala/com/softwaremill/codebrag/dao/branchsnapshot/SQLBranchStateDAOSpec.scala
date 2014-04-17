package com.softwaremill.codebrag.dao.branchsnapshot

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.test.{ClearSQLDataAfterTest, FlatSpecWithSQL}
import com.softwaremill.codebrag.domain.BranchState

trait BranchStateDAOSpec extends FlatSpec with ShouldMatchers {

  def branchStateDao: BranchStateDAO
  
  val MasterBranchName = "refs/remotes/origin/master"
  val FeatureBranchName = "refs/remotes/origin/feature"

  val MasterSHA = "123123123"
  val FeatureSHA = "456456456"

  it should "store state for given branch" in {
    // given
    val state = BranchState(MasterBranchName, MasterSHA)

    // when
    branchStateDao.storeBranchState(state)
    
    // then
    val Some(loaded) = branchStateDao.loadBranchState(MasterBranchName)
    loaded should equal(state)
  }

  it should "overwrite state for given branch" in {
    // given
    val oldState = BranchState(MasterBranchName, MasterSHA)
    branchStateDao.storeBranchState(oldState)

    val newState = BranchState(MasterBranchName, "456456456")
    branchStateDao.storeBranchState(newState)

    // then
    val Some(loaded) = branchStateDao.loadBranchState(MasterBranchName)
    loaded should equal(newState)
  }

  it should "not find state for not existing branch" in {
    // given
    val fakeBranchName = "NonExistingBranchName"

    // given
    val loaded = branchStateDao.loadBranchState(fakeBranchName)

    // then
    loaded should be('empty)
  }

  it should "fetch all branches' states" in {
    // given
    val masterState = BranchState(MasterBranchName, MasterSHA)
    val featureState = BranchState(FeatureBranchName, FeatureSHA)
    List(masterState, featureState).foreach(branchStateDao.storeBranchState)

    // when
    val allStates = branchStateDao.loadBranchesState

    // then
    allStates should be(Set(masterState, featureState))
  }
  
  it should "load all branches' states as map of branch name and sha" in {
    // given
    val masterState = BranchState(MasterBranchName, MasterSHA)
    val featureState = BranchState(FeatureBranchName, FeatureSHA)
    List(masterState, featureState).foreach(branchStateDao.storeBranchState)

    // when
    val allStates = branchStateDao.loadBranchesStateAsMap

    // then
    val expectedStatesMap = Map(MasterBranchName -> MasterSHA, FeatureBranchName -> FeatureSHA)
    allStates should be(expectedStatesMap)

  }

  it should "remove given branches" in {
    // given
    val masterState = BranchState(MasterBranchName, MasterSHA)
    val featureState = BranchState(FeatureBranchName, FeatureSHA)
    List(masterState, featureState).foreach(branchStateDao.storeBranchState)

    // when
    branchStateDao.removeBranches(Set(FeatureBranchName, "NonExistingBranch"))

    // then
    val allStates = branchStateDao.loadBranchesStateAsMap
    val expectedStatesMap = Map(MasterBranchName -> MasterSHA)
    allStates should be(expectedStatesMap)
  }

}

class SQLBranchStateDAOSpec extends FlatSpecWithSQL with ClearSQLDataAfterTest with BranchStateDAOSpec {
  var branchStateDao = new SQLBranchStateDAO(sqlDatabase)
}
