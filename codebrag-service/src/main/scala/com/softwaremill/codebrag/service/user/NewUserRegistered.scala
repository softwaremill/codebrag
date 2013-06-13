package com.softwaremill.codebrag.service.user

import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.Event

case class NewUserRegistered(id: ObjectId, login: String, fullName: String, email: String) extends Event