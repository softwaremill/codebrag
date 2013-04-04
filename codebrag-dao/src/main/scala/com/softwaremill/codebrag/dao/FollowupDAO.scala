package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.Followup

trait FollowupDAO {

  def createOrUpdateExisting(followup: Followup)

}
