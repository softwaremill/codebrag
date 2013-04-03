package com.softwaremill.codebrag.dao

import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.FollowUp
import org.joda.time.DateTime

class MongoFollowUpDAOSpec extends FlatSpecWithRemoteMongo with BeforeAndAfterEach with ShouldMatchers {

  var followUpDAO: MongoFollowUpDAO = _

  val Commit = CommitInfoBuilder.createRandomCommit()
  val FollowUpTargetUserId = ObjectIdTestUtils.oid(12)

  override def beforeEach() {
    followUpDAO = new MongoFollowUpDAO
    FollowUpRecord.drop
  }

  behavior of "MongoFollowUpDAO"

  it should "create and store follow up for user and commit" in {
    // given
    val now = DateTime.now()
    val followUp = FollowUp(Commit, FollowUpTargetUserId, now)

    // when
    followUpDAO.create(followUp)

    // then
    val allRecords = FollowUpRecord.findAll
    allRecords should have size(1)
    allRecords(0).user_id.toString() should equal(FollowUpTargetUserId.toString)  // TODO: how to match without toString
    // TODO: more assertions, how to match against nested record .commit ?. how to match dates?
  }

}
