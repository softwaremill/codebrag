package com.softwaremill.codebrag.domain

import com.softwaremill.codebrag.common.Event

/**
 * Event emitted when followup is created/updated as a result of user's action (comment/like)
 * @param followup followup generated
 */
case class FollowupForUserCreatedEvent(followup: Followup) extends Event
