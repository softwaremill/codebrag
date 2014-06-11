package com.softwaremill.codebrag.dao.branchsnapshot

import com.softwaremill.codebrag.test.{ClearSQLDataAfterTest, FlatSpecWithSQL}
import com.softwaremill.codebrag.domain.BranchState
import org.scalatest.matchers.ShouldMatchers

class BranchStateDAOSpec extends FlatSpecWithSQL with ClearSQLDataAfterTest with ShouldMatchers {

  var branchStateDao = new SQLBranchStateDAO(sqlDatabase)
  
  val MasterBranchName = "refs/remotes/origin/master"
  val FeatureBranchName = "refs/remotes/origin/feature"

  val MasterSHA = "123123123"
  val FeatureSHA = "456456456"
  val CodebragRepo = "codebrag"
  val AnotherRepo = "secret_project"

  it should "store state for given branch and repo" in {
    // given
    val codebragBranchState = BranchState(CodebragRepo, MasterBranchName, MasterSHA)
    val anotherProjectBranchState = BranchState(AnotherRepo, FeatureBranchName, FeatureSHA)

    // when
    branchStateDao.storeBranchState(codebragBranchState)
    branchStateDao.storeBranchState(anotherProjectBranchState)
    
    // then
    val Some(codebragBranch) = branchStateDao.loadBranchState(CodebragRepo, MasterBranchName)
    val Some(anotherProjectBranch) = branchStateDao.loadBranchState(AnotherRepo, FeatureBranchName)
    codebragBranch should equal(codebragBranchState)
    anotherProjectBranch should equal(anotherProjectBranchState)
  }

  it should "overwrite state for given branch for repo" in {
    // given
    val oldState = BranchState(CodebragRepo, MasterBranchName, MasterSHA)
    branchStateDao.storeBranchState(oldState)

    val newState = BranchState(CodebragRepo, MasterBranchName, "456456456")
    branchStateDao.storeBranchState(newState)

    // then
    val Some(loaded) = branchStateDao.loadBranchState(CodebragRepo, MasterBranchName)
    loaded should equal(newState)
  }

  it should "save branch with same name for different repos" in {
    // given
    val codebragFeatureBranch = BranchState(CodebragRepo, FeatureBranchName, FeatureSHA)
    val anotherProjectFeatureBranch = BranchState(AnotherRepo, FeatureBranchName, FeatureSHA)

    // when
    branchStateDao.storeBranchState(codebragFeatureBranch)
    branchStateDao.storeBranchState(anotherProjectFeatureBranch)

    // then
    branchStateDao.loadBranchesState(CodebragRepo).size should be(1)
    branchStateDao.loadBranchesState(AnotherRepo).size should be(1)
  }

  it should "not find state for not existing branch and repo" in {
    // given
    val state = BranchState(CodebragRepo, MasterBranchName, MasterSHA)
    branchStateDao.storeBranchState(state)
    val nonExistingBranch = "NonExistingBranch"
    val nonExistingRepo = "NonExistingRepo"

    // given
    val nonExistingBranchResult = branchStateDao.loadBranchState(CodebragRepo, nonExistingBranch)
    val wrongRepoResult = branchStateDao.loadBranchState(nonExistingRepo, MasterBranchName)

    // then
    nonExistingBranchResult should be('empty)
    wrongRepoResult should be('empty)
  }

  it should "fetch all branches' states for given repo only" in {
    // given
    val masterState = BranchState(CodebragRepo, MasterBranchName, MasterSHA)
    val featureState = BranchState(CodebragRepo, FeatureBranchName, FeatureSHA)
    List(masterState, featureState).foreach(branchStateDao.storeBranchState)
    val anotherRepoState = BranchState(AnotherRepo, FeatureBranchName, FeatureSHA)
    branchStateDao.storeBranchState(anotherRepoState)

    // when
    val allStates = branchStateDao.loadBranchesState(CodebragRepo)

    // then
    allStates should be(Set(masterState, featureState))
  }
  
  it should "load all branches' states as map of branch name and sha for given repo" in {
    // given
    val masterState = BranchState(CodebragRepo, MasterBranchName, MasterSHA)
    val featureState = BranchState(CodebragRepo, FeatureBranchName, FeatureSHA)
    List(masterState, featureState).foreach(branchStateDao.storeBranchState)

    // when
    val allStates = branchStateDao.loadBranchesStateAsMap(CodebragRepo)

    // then
    val expectedStatesMap = Map(MasterBranchName -> MasterSHA, FeatureBranchName -> FeatureSHA)
    allStates should be(expectedStatesMap)

  }

  it should "remove given branches for given repo" in {
    // given
    val codebragMasterState = BranchState(CodebragRepo, MasterBranchName, MasterSHA)
    val codebragFeatureState = BranchState(CodebragRepo, FeatureBranchName, FeatureSHA)
    List(codebragMasterState, codebragFeatureState).foreach(branchStateDao.storeBranchState)

    val anotherRepoFeatureState = BranchState(AnotherRepo, FeatureBranchName, FeatureSHA)
    branchStateDao.storeBranchState(anotherRepoFeatureState)

    // when
    branchStateDao.removeBranches(CodebragRepo, Set(FeatureBranchName, "NonExistingBranch"))

    // then
    val codebragRepoBranchesStates = branchStateDao.loadBranchesStateAsMap(CodebragRepo)
    val expectedCodebragStatesMap = Map(MasterBranchName -> MasterSHA)
    codebragRepoBranchesStates should be(expectedCodebragStatesMap)

    val anotherRepoBranchesStates = branchStateDao.loadBranchesStateAsMap(AnotherRepo)
    val expectedAnotherRepoStatesMap = Map(FeatureBranchName -> FeatureSHA)
    anotherRepoBranchesStates should be(expectedAnotherRepoStatesMap)
  }

}