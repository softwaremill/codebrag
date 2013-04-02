package com.softwaremill.codebrag.dao

import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.{FollowUpStatus, Authentication, User, FollowUp}

class MongoFollowUpDAOSpec extends FlatSpecWithRemoteMongo with BeforeAndAfterEach with ShouldMatchers {

  var followUpDAO: MongoFollowUpDAO = _

  override def beforeEach() {
    followUpDAO = new MongoFollowUpDAO
    FollowUpRecord.drop
  }

  behavior of "MongoFollowUpDAO"

  it should "create and store follow up for user and commit" in {
    // given
    val commit = CommitInfoBuilder.createRandomCommit()
    val user = User(Authentication.basic("mostr", "12345"), "mostr", "mostr@sml.com", "12345")
    val followUp = FollowUp(commit, user, FollowUpStatus.New)

    // when
    followUpDAO.create(followUp)

    // then
    val allRecords = FollowUpRecord.findAll
    allRecords should have size(1)  // TODO: check if commit and user data are the same as in followUp. How to assert on nested records?
  }

}
