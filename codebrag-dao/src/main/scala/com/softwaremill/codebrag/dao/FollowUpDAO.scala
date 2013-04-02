package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.FollowUp

trait FollowUpDAO {

  def create(followUp: FollowUp)

}
