package com.softwaremill.codebrag.domain

import org.bson.types.ObjectId

case class Invitation(code: String, invitationSender: ObjectId)
