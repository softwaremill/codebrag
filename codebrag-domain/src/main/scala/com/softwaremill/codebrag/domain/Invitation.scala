package com.softwaremill.codebrag.domain

import org.bson.types.ObjectId
import org.joda.time.DateTime

case class Invitation(code: String, invitationSender: ObjectId, expiryDate: DateTime = DateTime.now.plusHours(24))