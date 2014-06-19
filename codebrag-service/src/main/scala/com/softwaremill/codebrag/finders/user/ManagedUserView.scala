package com.softwaremill.codebrag.finders.user

import org.bson.types.ObjectId

case class ManagedUserView(userId: ObjectId, email: String, name: String, active: Boolean, admin: Boolean)
