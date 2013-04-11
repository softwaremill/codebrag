package com.softwaremill.codebrag.domain

import org.bson.types.ObjectId
import org.joda.time.DateTime


case class Followup(commitId: ObjectId, userId: ObjectId, date: DateTime)